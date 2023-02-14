// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import java.time.Instant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.NoteData;
import org.openstreetmap.josm.plugins.maproulette.api.enums.TaskStatus;
import org.openstreetmap.josm.tools.Pair;

/**
 * A MR task
 *
 * @param id                  The id of the task
 * @param name                The name of the task (or challenge)
 * @param created             The created date
 * @param modified            The last modified date
 * @param parentId            The parent id, if any
 * @param instruction         The instructions, if any
 * @param location            The general location of the task
 * @param geometries          The specific geometries of the task
 * @param cooperativeWork     The cooperative work string
 * @param status              The status of the task
 * @param mappedOn            The date the task was mapped
 * @param completedTimeSpent  The amount of time spent
 * @param completedBy         The user id that completed the task
 * @param review              Review information
 * @param priority            The priority of the task (default is "HIGH")
 * @param changesetId         The changeset where the task was (probably) fixed
 * @param completionResponses The response when the task was completed
 * @param bundleId            The bundle this task is part of
 * @param isBundlePrimary     If the bundle is a primary bundle
 * @param mapillaryImages     Mapillary images to show the user (probably broken since v3 -> v4 api)
 * @param errorTags           The error tags
 */
public record Task(long id, @Nonnull String name, @Nonnull Instant created, @Nonnull Instant modified, long parentId,
                   @Nullable String instruction,
                   @Nullable Point location, @Nonnull DataSet geometries, @Nullable Object cooperativeWork, @Nonnull TaskStatus status,
                   @Nullable Instant mappedOn, @Nullable Long completedTimeSpent, @Nullable Long completedBy, @Nonnull TaskReviewFields review,
                   int priority, @Nullable Long changesetId, @Nullable String completionResponses, @Nullable Long bundleId,
                   Boolean isBundlePrimary, @Nullable MapillaryImages mapillaryImages, @Nonnull String errorTags) implements TaskClusteredPoint {
    /**
     * Validate the objects for this record
     *
     * @param id                  The id of the task
     * @param name                The name of the task (or challenge)
     * @param created             The created date
     * @param modified            The last modified date
     * @param parentId            The parent id, if any
     * @param instruction         The instructions, if any
     * @param location            The general location of the task
     * @param geometries          The specific geometries of the task
     * @param cooperativeWork     The cooperative work string
     * @param status              The status of the task
     * @param mappedOn            The date the task was mapped
     * @param completedTimeSpent  The amount of time spent
     * @param completedBy         The user id that completed the task
     * @param review              Review information
     * @param priority            The priority of the task (default is "HIGH")
     * @param changesetId         The changeset where the task was (probably) fixed
     * @param completionResponses The response when the task was completed
     * @param bundleId            The bundle this task is part of
     * @param isBundlePrimary     If the bundle is a primary bundle
     * @param mapillaryImages     Mapillary images to show the user (probably broken since v3 -> v4 api)
     * @param errorTags           The error tags
     */
    public Task {
        if (status == null) {
            status = TaskStatus.CREATED;
        }
    }

    /**
     * Get the cooperative work as a change
     *
     * @return The change
     */
    public OSMChange cooperativeWorkAsOsmChange() {
        if (isCooperativeWorkOsmChange()) {
            return (OSMChange) this.cooperativeWork;
        }
        return null;
    }

    /**
     * Check and see if this is a cooperative work tag change task
     *
     * @return {@code true} if the work is a tag change
     */
    public boolean isCooperativeWorkOsmChange() {
        return this.cooperativeWork instanceof OSMChange;
    }

    /**
     * Get the cooperative work as an OSC
     *
     * @return The cooperative work
     */
    public Pair<DataSet, NoteData> cooperativeWorkAsOsc() {
        if (isCooperativeWorkOsc()) {
            return (Pair<DataSet, NoteData>) this.cooperativeWork;
        }
        return null;
    }

    /**
     * Check if the cooperative work is an OSC
     *
     * @return {@code true} if the work is an osc
     */
    public boolean isCooperativeWorkOsc() {
        return this.cooperativeWork instanceof Pair<?, ?> pair && pair.a instanceof DataSet && pair.b instanceof NoteData;
    }
}
