// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api;

import java.awt.Color;

import org.openstreetmap.josm.data.preferences.NamedColorProperty;
import org.openstreetmap.josm.plugins.maproulette.api.enums.TaskStatus;

import jakarta.annotation.Nullable;

/**
 * A class for getting colors
 */
public final class MRColors {
    /**
     * The color for fixed tasks
     */
    private static final NamedColorProperty FIXED = new NamedColorProperty("maproulette.status.fixed",
            new Color(101, 210, 218));
    /**
     * The color for false positive tasks
     */
    private static final NamedColorProperty FALSE_POSITIVE = new NamedColorProperty("maproulette.status.false_positive",
            new Color(247, 187, 89));
    /**
     * The color for already fixed tasks
     */
    private static final NamedColorProperty ALREADY_FIXED = new NamedColorProperty("maproulette.status.already_fixed",
            new Color(204, 177, 134));
    /**
     * The color for skipped tasks
     */
    private static final NamedColorProperty SKIPPED = new NamedColorProperty("maproulette.status.skipped",
            new Color(232, 124, 224));
    /**
     * The color for too hard tasks
     */
    private static final NamedColorProperty TOO_HARD = new NamedColorProperty("maproulette.status.too_hard",
            new Color(255, 94, 99));

    private MRColors() {
        // Hide the constructor
    }

    /**
     * Get a color for a specified status
     *
     * @param status The status to get the MR color for
     * @return The color to use
     */
    @Nullable
    public static Color statusColor(@Nullable TaskStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
        case FIXED -> FIXED.get();
        case FALSE_POSITIVE -> FALSE_POSITIVE.get();
        case ALREADY_FIXED -> ALREADY_FIXED.get();
        case SKIPPED -> SKIPPED.get();
        case TOO_HARD -> TOO_HARD.get();
        default -> null;
        };
    }
}
