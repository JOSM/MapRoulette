// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import org.openstreetmap.josm.plugins.maproulette.api.enums.Priority;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Calculations for challenge priority
 *
 * @param defaultPriority    The default priority
 * @param highPriorityRule   The rule to use to set high priority tasks
 * @param mediumPriorityRule The rule to use to set medium priority tasks
 * @param lowPriorityRule    The rule to use to set low priority tasks
 */
public record ChallengePriority(@Nonnull Priority defaultPriority, @Nullable String highPriorityRule, @Nullable String mediumPriorityRule,
                                @Nullable String lowPriorityRule) {
    /**
     * Instantiation-time checks
     *
     * @param defaultPriority    The default priority
     * @param highPriorityRule   The rule to use to set high priority tasks
     * @param mediumPriorityRule The rule to use to set medium priority tasks
     * @param lowPriorityRule    The rule to use to set low priority tasks
     */
    public ChallengePriority {
        if (defaultPriority == null) {
            defaultPriority = Priority.HIGH;
        }
    }
}
