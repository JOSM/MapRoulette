// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.enums;

/**
 * The roles of the user
 */
public enum Role {
    /**
     * A root user
     */
    SUPER_USER, // -1
    /**
     * An admin for a challenge/project
     */
    ADMIN, // 1
    /**
     * Someone with write access for a challenge/project
     */
    WRITE_ACCESS, // 2
    /**
     * Someone with read access for a challenge/project
     */
    READ_ONLY, // 3
}
