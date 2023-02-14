// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

/**
 * Public user information
 *
 * @param id          The OSM user id
 * @param avatarURL   The OSM avatar url
 * @param displayName The OSM display name
 */
public record PublicOSMUser(long id, String avatarURL, String displayName) implements Identifier {
}
