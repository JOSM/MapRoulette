// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.enums;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * The current status of a task
 */
public enum TaskStatus {
    /**
     * The task hasn't been modified since creation
     */
    CREATED, // 0
    /**
     * The task is fixed
     */
    FIXED, // 1
    /**
     * The task was a false positive
     */
    FALSE_POSITIVE, // 2
    /**
     * The task was skipped
     */
    SKIPPED, // 3
    /**
     * The task was deleted
     */
    DELETED, // 4
    /**
     * The task was already fixed
     */
    ALREADY_FIXED, // 5
    /**
     * The task was too hard
     */
    TOO_HARD, // 6
    /**
     * The task has had a comment
     */
    ANSWERED, // 7
    /**
     * The task is validated
     */
    VALIDATED, // 8
    /**
     * The task is disabled
     */
    DISABLED; // 9

    /**
     * Get the description to show the user
     *
     * @return The description for the user
     */
    public String description() {
        return switch (this) {
        case CREATED -> tr("Created");
        /* SHORTCUT(MapRoulette: Fixed, maproulette:fixed, NONE, UNDEFINED) */
        case FIXED -> tr("Fixed");
        case DELETED -> tr("Deleted");
        /* SHORTCUT(MapRoulette: Skipped, maproulette:skipped, NONE, UNDEFINED) */
        case SKIPPED -> tr("Skipped");
        case ANSWERED -> tr("Answered");
        case DISABLED -> tr("Disabled");
        /* SHORTCUT(MapRoulette: Too Hard / Cannot see, maproulette:too_hard, NONE, UNDEFINED) */
        case TOO_HARD -> tr("Too Hard / Cannot see");
        case VALIDATED -> tr("Validated");
        /* SHORTCUT(MapRoulette: Mark Task as Already Fixed / Not an Issue, maproulette:already_fixed, NONE, UNDEFINED) */
        case ALREADY_FIXED -> tr("Already Fixed / Not an Issue");
        /* SHORTCUT(MapRoulette: Mark Task as False Positive, maproulette:false_positive, NONE, UNDEFINED) */
        case FALSE_POSITIVE -> tr("False Positive");
        };
    }
}
