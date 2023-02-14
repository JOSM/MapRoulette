// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openstreetmap.josm.plugins.maproulette.api.enums.TaskStatus;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;

/**
 * Information for a modified task
 *
 * @param task            The task to modify
 * @param status          The status to set
 * @param comment         The comment to use
 * @param tags            The tags to use
 * @param reviewRequested Force review or not
 */
public record ModifiedTask(@Nonnull Task task, @Nonnull TaskStatus status, @Nullable String comment,
                           @Nullable String tags, @Nullable Boolean reviewRequested) {
    /**
     * Validate the non-null fields
     *
     * @param task            The task to modify
     * @param status          The status to set
     * @param comment         The comment to use
     * @param tags            The tags to use
     * @param reviewRequested Force review or not
     */
    public ModifiedTask {
        Objects.requireNonNull(task);
        Objects.requireNonNull(status);
    }
}
