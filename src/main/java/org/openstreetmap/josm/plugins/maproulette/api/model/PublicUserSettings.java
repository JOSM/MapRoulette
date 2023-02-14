// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

/**
 * Public user settings
 *
 * @param leaderboardOptOut Whether the user has opted out of being displayed on the MapRoulette leaderboard
 */
public record PublicUserSettings(boolean leaderboardOptOut) {
}
