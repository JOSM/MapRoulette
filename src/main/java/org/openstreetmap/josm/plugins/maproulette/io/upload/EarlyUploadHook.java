// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.io.upload;

import static org.openstreetmap.josm.plugins.maproulette.config.MapRouletteConfig.getBaseUrl;
import static org.openstreetmap.josm.plugins.maproulette.gui.task.current.CurrentTaskPanel.getSelections;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.text.html.HTMLDocument;

import org.openstreetmap.josm.actions.upload.UploadHook;
import org.openstreetmap.josm.data.APIDataSet;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.data.osm.Tagged;
import org.openstreetmap.josm.gui.ConditionalOptionPaneUtil;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.gui.widgets.JosmEditorPane;
import org.openstreetmap.josm.gui.widgets.VerticallyScrollablePanel;
import org.openstreetmap.josm.plugins.maproulette.api.TaskAPI;
import org.openstreetmap.josm.plugins.maproulette.api.enums.TaskStatus;
import org.openstreetmap.josm.plugins.maproulette.api.model.ClusteredPoint;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.plugins.maproulette.api.model.TaskClusteredPoint;
import org.openstreetmap.josm.plugins.maproulette.api_caching.ChallengeCache;
import org.openstreetmap.josm.plugins.maproulette.data.TaskPrimitives;
import org.openstreetmap.josm.plugins.maproulette.gui.MRGuiHelper;
import org.openstreetmap.josm.plugins.maproulette.gui.ModifiedObjects;
import org.openstreetmap.josm.plugins.maproulette.gui.ModifiedTask;
import org.openstreetmap.josm.plugins.maproulette.gui.TagChangeTable;
import org.openstreetmap.josm.plugins.maproulette.gui.layer.MapRouletteClusteredPointLayer;
import org.openstreetmap.josm.plugins.maproulette.util.ExceptionDialogUtil;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Utils;

/**
 * An early upload hook for setting the changeset tags (for review by user)
 */
public final class EarlyUploadHook implements UploadHook {
    /**
     * The base pref key for asking the user if a task is finished
     */
    static final String PREF_CHECK_IF_FINISHED = "maproulette.check_if_finished";
    private static final int MAX_LONG_STRING_LENGTH = Long.toString(Long.MAX_VALUE).length();
    /**
     * The base number predicate
     */
    private static final Predicate<String> NUMBER_PREDICATE = Pattern.compile("^\\d+$").asMatchPredicate();
    /**
     * The predicate for simple primitive ids
     */
    private static final Predicate<String> ID_PREDICATE = SimplePrimitiveId.ID_PATTERN.asMatchPredicate();
    /**
     * The pattern for ids with a version
     */
    private static final Pattern VERSION_PATTERN = Pattern
            .compile("(n|node|w|way|r|rel|relation)[ /]?(\\d+)[ v@]?(\\d+)");
    /**
     * The pattern for id_type_version
     */
    private static final Pattern ID_TYPE_VERSION_PATTERN = Pattern.compile("(\\d+)_(n|node|w|way|r|rel|relation)_\\d+");

    @Override
    public boolean checkUpload(APIDataSet apiDataSet) {
        final var ids = apiDataSet.getPrimitives().stream().map(IPrimitive::getPrimitiveId).collect(Collectors.toSet());
        //noinspection DataFlowIssue -- getPrimitiveId should stably return either a null value or a non-null value
        final var tasks = MainApplication.getLayerManager().getLayersOfType(MapRouletteClusteredPointLayer.class)
                .stream().map(MapRouletteClusteredPointLayer::getTasks).flatMap(Collection::stream)
                .filter(p -> getPrimitiveId(p) != null).collect(Collectors.groupingBy(EarlyUploadHook::getPrimitiveId));
        final var possiblyDone = ids.stream().filter(id -> tasks.containsKey(id.getUniqueId()))
                .collect(Collectors.toMap(PrimitiveId::getUniqueId, p -> p));
        final var exceptionList = new ArrayList<Exception>();
        final var possibleTasks = tasks.entrySet().stream().filter(t -> possiblyDone.containsKey(t.getKey()))
                .map(Map.Entry::getValue).flatMap(Collection::stream).mapToLong(TaskClusteredPoint::id)
                .filter(id -> ModifiedObjects.getModifiedTask(id) == null).mapToObj(id -> {
                    try {
                        return TaskAPI.get(id);
                    } catch (IOException e) {
                        Logging.trace(e);
                        exceptionList.add(e);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet());
        ConditionalOptionPaneUtil.startBulkOperation(PREF_CHECK_IF_FINISHED);
        for (var task : possibleTasks) {
            if (ids.containsAll(TaskPrimitives.getPrimitiveIds(task))) {
                final var descriptivePanel = createDescriptivePanel(task, apiDataSet);
                final var didFix = ConditionalOptionPaneUtil.showConfirmationDialog(PREF_CHECK_IF_FINISHED,
                        MainApplication.getMainFrame(), descriptivePanel,
                        tr("Did you finish the following MapRoulette Task: {0}?", task.id()),
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_OPTION);
                if (didFix) {
                    final var doc = (HTMLDocument) ((JosmEditorPane) descriptivePanel.getComponent(1)).getDocument();
                    ModifiedObjects.addModifiedTask(
                            new ModifiedTask(task, TaskStatus.FIXED, null, null, null, getSelections(doc)));
                }
            }
        }
        GuiHelper.runInEDT(() -> exceptionList.forEach(ExceptionDialogUtil::explainException));
        ConditionalOptionPaneUtil.endBulkOperation(PREF_CHECK_IF_FINISHED);
        return UploadHook.super.checkUpload(apiDataSet);
    }

    private static Long getPrimitiveId(TaskClusteredPoint point) {
        final String toParse;
        if (point instanceof ClusteredPoint clusteredPoint) {
            toParse = clusteredPoint.title();
        } else if (point instanceof Task task) {
            toParse = task.name();
        } else {
            throw new IllegalArgumentException("Unknown class type: " + point.getClass());
        }
        if (toParse.length() <= MAX_LONG_STRING_LENGTH + 6 && NUMBER_PREDICATE.test(toParse)
                && toParse.endsWith("000000")) {
            try {
                final var challenge = ChallengeCache.challenge(point.parentId());
                if (challenge.general().checkinComment() != null
                        && challenge.general().checkinComment().contains("#AtlasCheck")) {
                    return Long.parseLong(toParse.substring(0, toParse.length() - 6));
                }
            } catch (IOException ioException) {
                ExceptionDialogUtil.explainException(ioException);
            }
        }
        if (toParse.length() <= MAX_LONG_STRING_LENGTH && NUMBER_PREDICATE.test(toParse)) {
            return Long.parseLong(toParse);
        } else if (ID_PREDICATE.test(toParse)) {
            return SimplePrimitiveId.fromString(toParse).getUniqueId();
        }
        final var versionMatcher = VERSION_PATTERN.matcher(toParse);
        if (versionMatcher.matches()) {
            return Long.parseLong(versionMatcher.group(2));
        }
        final var idTypeMatcher = ID_TYPE_VERSION_PATTERN.matcher(toParse);
        if (idTypeMatcher.matches()) {
            return Long.parseLong(idTypeMatcher.group(1));
        } else if (point instanceof Task task) {
            final var ids = TaskPrimitives.getPrimitiveIds(task);
            if (ids.size() == 1) {
                return ids.iterator().next().getUniqueId();
            }
        }
        return null;
    }

    private static JPanel createDescriptivePanel(Task task, APIDataSet dataSet) {
        final var panel = new VerticallyScrollablePanel(new GridBagLayout());
        final var gbc = GBC.eol().fill(GBC.HORIZONTAL);
        final var instructionPane = new JosmEditorPane();
        JosmEditorPane.makeJLabelLike(instructionPane, false);
        instructionPane.setText(MRGuiHelper.getInstructionText(task));
        panel.add(new JLabel(tr("Instructions:")), gbc);
        panel.add(instructionPane, gbc);
        final var map = TaskPrimitives.getPrimitiveIdMap(task);
        for (var taskPrimitive : task.geometries().allPrimitives()) {
            if (taskPrimitive.isTagged()) {
                final var id = map.entrySet().stream().filter(entry -> entry.getValue() == taskPrimitive)
                        .map(Map.Entry::getKey).findFirst().orElse(null);
                final var osmPrimitive = id == null ? null
                        : dataSet.getPrimitives().stream()
                                .filter(p -> id.getType() == p.getType() && id.getUniqueId() == p.getUniqueId())
                                .findFirst().orElse(null);
                if (id != null && osmPrimitive != null) {
                    final var table = new TagChangeTable(false);
                    final List<String> keys = Stream
                            .concat(osmPrimitive.keys(), MRGuiHelper.filterKeys(taskPrimitive.keys())).sorted()
                            .distinct().toList();
                    for (var row = 0; row < keys.size(); row++) {
                        final var key = keys.get(row);
                        table.setValueAt(key, row, 0);
                        table.setValueAt(taskPrimitive.get(key), row, 1);
                        table.setValueAt(osmPrimitive.get(key), row, 2);
                    }
                    panel.add(new JSeparator(), gbc);
                    panel.add(new JLabel(tr("Changes for {0}", id)), gbc);
                    panel.add(new JScrollPane(table), gbc);
                }
            }
        }
        return panel;
    }

    @Override
    public void modifyChangesetTags(Map<String, String> tags) {
        UploadHook.super.modifyChangesetTags(tags);
        final var tagBuilders = new ArrayList<StringBuilder>();
        final var changesetComments = new TreeSet<String>();
        final var sourceComments = new TreeSet<String>();
        var tagBuilder = new StringBuilder();
        tagBuilders.add(tagBuilder);
        for (ModifiedTask entry : ModifiedObjects.getModifiedTasks()) {
            final var id = Long.toString(entry.task().id());
            if (tagBuilder.length() + id.length() + 1 >= Tagged.MAX_TAG_LENGTH) {
                tagBuilder = new StringBuilder();
                tagBuilders.add(tagBuilder);
            }
            if (!tagBuilder.isEmpty()) {
                tagBuilder.append(';');
            }
            tagBuilder.append(entry.task().id());
            try {
                final var challengeGeneral = ChallengeCache.challenge(entry.task().parentId()).general();
                if (!Utils.isStripEmpty(challengeGeneral.checkinComment())) {
                    changesetComments.add(challengeGeneral.checkinComment());
                }
                if (!Utils.isStripEmpty(challengeGeneral.checkinSource())) {
                    sourceComments.add(challengeGeneral.checkinSource());
                }
            } catch (IOException ioException) {
                ExceptionDialogUtil.explainException(ioException);
            }
        }

        if (!changesetComments.isEmpty()) {
            tags.put("comment", changesetComments.stream().sorted().collect(Collectors.joining("; ")));
        }
        if (!sourceComments.isEmpty()) {
            tags.put("source", sourceComments.stream().sorted().collect(Collectors.joining("; ")));
        }
        final var taskStrings = tagBuilders.stream().map(StringBuilder::toString).filter(str -> !str.isBlank())
                .toList();
        if (!taskStrings.isEmpty()) {
            tags.put("maproulette:tasks", taskStrings.get(0));
            tags.put("maproulette:server", getBaseUrl());
            for (var i = 1; i < taskStrings.size(); i++) {
                tags.put("maproulette:tasks:" + (i + 1), taskStrings.get(i));
            }
        }
    }

}
