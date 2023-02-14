// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.enums;

/**
 * The status of a review
 */
public enum ReviewStatus {
    /**
     * A review was not requested
     */
    NOT_REQUESTED, // -1
    /**
     * A review was requested
     */
    REQUESTED, // 0
    /**
     * The task was approved
     */
    APPROVED, // 1
    /**
     * The task was rejected
     */
    REJECTED, // 2
    /**
     * The task was approved but had assistance
     */
    ASSISTED, // 3
    /**
     * The task rejection status is disputed
     */
    DISPUTED, // 4
    /**
     * The task did not need to be reviewed
     */
    UNNECESSARY, // 5
    /**
     * The task was approved after revisions
     */
    APPROVED_WITH_REVISIONS, // 6
    /**
     * THe task was approved after revisions but still needed fixes
     */
    APPROVED_WITH_FIXES_AFTER_REVISIONS, // 7
}
