// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import java.time.Instant;

import org.openstreetmap.josm.plugins.maproulette.api.enums.Achievement;

/**
 * A user object
 *
 * @param id               The id of the user
 * @param created          the date that the user was created
 * @param modified         The date that the user was modified
 * @param osmProfile       The osm profile for the user
 * @param grants           Any grants for the user
 * @param apiKey           The MR api key for the user
 * @param guest            {@code true} if the user is a guest
 * @param settings         The settings for the user
 * @param properties       misc properties for the user
 * @param score            The user score
 * @param followingGroupId The group the user uses for following others
 * @param followersGroupId The group that others use to follow this user
 * @param achievements     The achievements for this user
 */
public record User(long id, Instant created, Instant modified, OSMProfile osmProfile, Grant[] grants,
                   String apiKey, boolean guest, UserSettings settings, String properties, int score,
                   long followingGroupId, long followersGroupId, Achievement[] achievements) implements BaseUser, Identifier {
}
