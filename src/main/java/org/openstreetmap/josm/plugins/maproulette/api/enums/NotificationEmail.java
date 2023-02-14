// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.enums;

/**
 * How the user has decided to recieve emails from MR
 */
public enum NotificationEmail {
    /**
     * They don't want any
     */
    IGNORE, // 0
    /**
     * They don't want any
     */
    NONE, // 1
    /**
     * They want their emails ASAP
     */
    IMMEDIATE, // 2
    /**
     * They want their emails sent in a digest
     */
    DIGEST, // 3
    /**
     * The email has been sent (?)
     */
    SENT, // 4
    /**
     * They want their emails sent daily
     */
    DAILY, // 5
    /**
     * They want their emails sent weekly
     */
    WEEKLY, // 6
}
