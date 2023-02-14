// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.enums;

/**
 * The status of a challenge
 */
public enum ChallengeStatus {
    /**
     * unknown challenge status
     */
    NA, // 0
    /**
     * The challenge is not yet ready
     */
    BUILDING, // 1
    /**
     * The challenge failed during processing
     */
    FAILED, // 2
    /**
     * The challenge is ready for mappers
     */
    READY, // 3
    /**
     * THe challenge is partially ready for users
     */
    PARTIALLY_LOADED, // 4
    /**
     * The challenge is finished
     */
    FINISHED, // 5
    /**
     * The challenge is currently deleting tasks
     */
    DELETING_TASKS, // 6
}
