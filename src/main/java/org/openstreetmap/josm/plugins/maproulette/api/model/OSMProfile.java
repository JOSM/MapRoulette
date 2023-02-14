// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import java.time.Instant;

/**
 * OSM profile information
 *
 * @param id           The OSM id
 * @param displayName  The current user-friendly name of the user
 * @param description  The description/blurb set by the user
 * @param avatarURL    The link to the user's avatar
 * @param homeLocation The home location of the user
 * @param created      The time that the user was created at
 * @param requestToken The token for the user (MR?)
 */
public record OSMProfile(long id, String displayName, String description, String avatarURL, Location homeLocation,
                         Instant created, String requestToken) implements Identifier {
}
