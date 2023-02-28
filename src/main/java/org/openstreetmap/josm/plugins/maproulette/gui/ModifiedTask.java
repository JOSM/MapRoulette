// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui;

import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.text.html.Option;

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
 * @param completionResponses Completion responses to send
 */
public record ModifiedTask(@Nonnull Task task, @Nonnull TaskStatus status, @Nullable String comment,
                           @Nullable String tags, @Nullable Boolean reviewRequested, @Nullable Map<String, Option> completionResponses) {
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
