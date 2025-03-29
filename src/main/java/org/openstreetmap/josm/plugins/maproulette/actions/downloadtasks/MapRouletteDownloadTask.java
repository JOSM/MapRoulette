// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.actions.downloadtasks;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.openstreetmap.josm.actions.downloadtasks.AbstractDownloadTask;
import org.openstreetmap.josm.actions.downloadtasks.DownloadParams;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressTaskId;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.plugins.maproulette.api.ChallengeAPI;
import org.openstreetmap.josm.plugins.maproulette.api.TaskAPI;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.plugins.maproulette.api.model.TaskClusteredPoint;
import org.openstreetmap.josm.plugins.maproulette.api_caching.ChallengeCache;
import org.openstreetmap.josm.plugins.maproulette.gui.layer.MapRouletteClusteredPointLayer;

/**
 * A download task for downloading specific tasks
 */
public class MapRouletteDownloadTask extends AbstractDownloadTask<Collection<Task>> {
    private static final Pattern PATTERN_CHALLENGE_TASK = Pattern.compile(".*/challenge/(\\d+)/task/(\\d+)");
    private static final Pattern PATTERN_CHALLENGE = Pattern.compile(".*/challenges/(\\d+)");
    private final long challenge;
    private final long task;
    private PleaseWaitRunnable downloadTask;

    /**
     * The default constructor
     */
    public MapRouletteDownloadTask() {
        this(-1, -1);
    }

    /**
     * Download tasks from a specific challenge and optionally around the specified task
     * @param challenge The challenge to download from
     * @param task The task to download around
     */
    public MapRouletteDownloadTask(long challenge, long task) {
        this.challenge = challenge;
        this.task = task;
    }

    @Override
    public Future<?> download(DownloadParams settings, Bounds downloadArea, ProgressMonitor progressMonitor) {
        return null;
    }

    @Override
    public Future<?> loadUrl(DownloadParams settings, String url, ProgressMonitor progressMonitor) {
        final var challengeTaskMatcher = PATTERN_CHALLENGE_TASK.matcher(url);
        if (challengeTaskMatcher.matches()) {
            final var taskId = challengeTaskMatcher.group(2);
            this.downloadTask = new DownloadTask(progressMonitor, Long.parseLong(taskId));
            return MainApplication.worker.submit(this.downloadTask);
        }
        final var challengeMatcher = PATTERN_CHALLENGE.matcher(url);
        if (challengeMatcher.matches()) {
            final var challengeId = Long.parseLong(challengeMatcher.group(1));
            this.downloadTask = new DownloadChallenge(progressMonitor, challengeId, -1);
            return MainApplication.worker.submit(this.downloadTask);
        }
        if (challenge > 0) {
            this.downloadTask = new DownloadChallenge(progressMonitor, challenge, task);
            return MainApplication.worker.submit(this.downloadTask);
        }
        return null;
    }

    @Override
    public void cancel() {
        if (this.downloadTask instanceof DownloadChallenge c) {
            c.cancel();
        } else if (this.downloadTask instanceof DownloadTask t) {
            t.cancel();
        } else {
            throw new UnsupportedOperationException("Cannot cancel " + this.downloadTask);
        }
    }

    @Override
    public String getConfirmationMessage(URL url) {
        if (PATTERN_CHALLENGE_TASK.matcher(url.toExternalForm()).matches()) {
            return tr("Download task {0}", url.toExternalForm());
        }
        if (PATTERN_CHALLENGE.matcher(url.toExternalForm()).matches()) {
            return tr("Download challenge {0}", url.toExternalForm());
        }
        throw new IllegalArgumentException("Unsupported url: " + url);
    }

    @Override
    public String[] getPatterns() {
        return new String[] { "https?://.*" + PATTERN_CHALLENGE_TASK.pattern(),
                "https?://.*" + PATTERN_CHALLENGE.pattern() };
    }

    @Override
    public String getTitle() {
        return tr("Download MapRoulette Task");
    }

    private abstract static class DownloadParent extends PleaseWaitRunnable {
        protected Collection<Task> tasks;
        protected boolean isCancelled;

        protected DownloadParent(String title, ProgressMonitor progressMonitor, boolean ignoreException) {
            super(title, progressMonitor, ignoreException);
        }

        @Override
        protected void cancel() {
            this.isCancelled = true;
        }

        @Override
        protected void finish() {
            if (tasks != null && !tasks.isEmpty()) {
                final var taskList = new ArrayList<>(tasks);
                final var tcMap = taskList.stream().collect(Collectors.toMap(Task::id, i -> i));
                final var currentLayers = MainApplication.getLayerManager()
                        .getLayersOfType(MapRouletteClusteredPointLayer.class);
                final var initialPoint = taskList.get(0).location();
                Objects.requireNonNull(initialPoint, "Task did not have location: " + taskList.get(0).id());
                final var bounds = new Bounds(initialPoint.lat(), initialPoint.lon(), initialPoint.lat(),
                        initialPoint.lon());
                if (currentLayers.isEmpty()) {
                    final var layer = new MapRouletteClusteredPointLayer(bounds, List.copyOf(taskList));
                    MainApplication.getLayerManager().addLayer(layer);
                    MainApplication.getMap().mapView.addMouseListener(layer);
                } else {
                    currentLayers.forEach(layer -> layer.refreshTasks(tcMap));
                    final var newTcMap = taskList.stream()
                            .collect(Collectors.toMap(TaskClusteredPoint::id, Function.identity()));
                    currentLayers.forEach(layer -> layer.refreshTasks(newTcMap));
                }
            }
        }
    }

    private static class DownloadChallenge extends DownloadParent {
        private static final ProgressTaskId PROGRESS_TASK_ID = new ProgressTaskId("maproulette", "download challenge");
        private final long challengeId;
        private final long task;

        /**
         * Create a new download task
         *
         * @param progressMonitor The progress monitor to use
         * @param id              The id to download
         * @param task            The optional task to get tasks around
         */
        DownloadChallenge(ProgressMonitor progressMonitor, long id, long task) {
            super(tr("Downloading MapRoulette Challenge {0}", id), progressMonitor, false);
            this.challengeId = id;
            this.task = task;
        }

        @Override
        protected void realRun() throws IOException {
            this.tasks = new ArrayList<>();
            final var challenge = ChallengeCache.challenge(this.challengeId);
            if (!this.isCancelled) {
                if (challenge.tasksRemaining() != null && challenge.tasksRemaining() > 0) {
                    this.tasks
                            .addAll(Arrays.asList(ChallengeAPI.randomTask(challenge.id(), null, null, 10, this.task)));
                } else {
                    GuiHelper.runInEDT(() -> new Notification(tr("Challenge may be done")).show());
                }
            }
        }

        @Override
        public ProgressTaskId canRunInBackground() {
            return PROGRESS_TASK_ID;
        }
    }

    private static class DownloadTask extends DownloadParent {
        private static final ProgressTaskId PROGRESS_TASK_ID = new ProgressTaskId("maproulette", "download task");
        private final long[] taskIds;

        /**
         * Create a new download task
         *
         * @param progressMonitor The progress monitor to use
         * @param ids             The ids to download
         */
        DownloadTask(ProgressMonitor progressMonitor, long... ids) {
            super(tr("Downloading MapRoulette Task {0}", ids[0]), progressMonitor, false);
            this.taskIds = ids;
        }

        @Override
        protected void realRun() throws IOException {
            this.tasks = new ArrayList<>();
            for (var taskId : this.taskIds) {
                if (this.isCancelled) {
                    return;
                }
                final var task = TaskAPI.get(taskId);
                this.tasks.add(task);
                ChallengeCache.challenge(task.parentId());
            }
        }

        @Override
        public ProgressTaskId canRunInBackground() {
            return PROGRESS_TASK_ID;
        }
    }
}
