// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import java.io.IOException;
import java.time.Instant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.plugins.maproulette.api.TaskAPI;
import org.openstreetmap.josm.plugins.maproulette.api.enums.Difficulty;
import org.openstreetmap.josm.plugins.maproulette.api.enums.TaskStatus;


/**
 * Another representation of a {@link Task} or a {@link Challenge}
 *
 * @param id                 The id
 * @param owner              The owner id
 * @param ownerName          The owner name
 * @param title              The title of the task
 * @param parentId           The id of the parent
 * @param parentName         The name of the parent
 * @param location           The location of the task
 * @param bounding
 * @param blurb              The first line of the task instructions
 * @param modified           When the task was last modified
 * @param difficulty         The difficulty (only if a challenge)
 * @param type               The task type
 * @param status             The status of the task (not used on challenge points)
 * @param cooperativeWork    The cooperative work task as a string instead of object
 * @param mappedOn           The time the task was mapped
 * @param completedTimeSpent The amount of time from when the task was locked to when it was mapped
 * @param completedBy        Who completed the task
 * @param pointReview
 * @param priority           The priority of the task
 * @param bundleId           The bundle for this task
 * @param isBundlePrimary    If the bundle is the primary bundle
 */
public record ClusteredPoint(long id, long owner, @Nonnull String ownerName, String title, long parentId, @Nonnull String parentName,
                             @Nonnull Point location, @Nonnull String bounding, @Nonnull String blurb, @Nonnull Instant modified,
                             @Nullable Difficulty difficulty, int type, @Nullable TaskStatus status, @Nullable String cooperativeWork,
                             @Nullable Instant mappedOn, @Nullable Long completedTimeSpent, @Nullable BaseUser completedBy,
                             PointReview pointReview, int priority, @Nullable Long bundleId, @Nullable Boolean isBundlePrimary)
        implements TaskClusteredPoint {

    /**
     * Get the task for this point.
     *
     * @return The task.
     * @throws IOException if there was a problem communicating with the server
     */
    public Task task() throws IOException {
        return TaskAPI.get(this.id());
    }

    @Override
    public BBox getBBox() {
        return this.location().getBBox();
    }
}
