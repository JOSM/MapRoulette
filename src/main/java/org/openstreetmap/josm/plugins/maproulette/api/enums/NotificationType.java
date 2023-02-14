// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.enums;

/**
 * Notification types
 */
public enum NotificationType {
    /**
     * Notice is from system
     */
    SYSTEM, // 0
    /**
     * Someone else mentioned the user
     */
    MENTION, // 1
    /**
     * Someone approved a review
     */
    REVIEW_APPROVED, // 2
    /**
     * Someone rejected a review
     */
    REVIEW_REJECTED, // 3
    /**
     * Someone requested that a task be reviewed again
     */
    REVIEW_AGAIN, // 4
    /**
     * A challenge has been completed
     */
    CHALLENGE_COMPLETED, // 5
    /**
     * A message from a team
     */
    TEAM, // 6
    /**
     * Someone followed the user
     */
    FOLLOW, // 7
    /**
     * One of the users' challenges has been completed
     */
    MAPPER_CHALLENGE_COMPLETED, // 8
    /**
     * Someone revised their review
     */
    REVIEW_REVISED, // 9
    /**
     * A meta review has been performed
     */
    META_REVIEW, // 10
    /**
     * A meta review has been performed again
     */
    META_REVIEW_AGAIN, // 11
    /**
     * Some number of reviews have been performed
     */
    REVIEW_COUNT, // 12
    /**
     * Some number of revisions have been performed
     */
    REVISION_COUNT, // 13
    /**
     * Someone commented on a challenge
     */
    CHALLENGE_COMMENT, // 14
}
