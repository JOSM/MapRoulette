// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import java.time.Instant;

/**
 * Public user information
 *
 * @param id         The MR id of the user
 * @param osmProfile The osm profile for the user
 * @param name       The visible name for the user
 * @param created    The time the user was created
 * @param settings   any visible public settings
 */
public record PublicUser(long id, PublicOSMUser osmProfile, String name, Instant created,
                         PublicUserSettings settings) implements BaseUser, Identifier {
}
