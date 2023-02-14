// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import javax.annotation.Nullable;

/**
 * The settings for the user
 *
 * @param defaultEditor        the default editor for the user
 * @param defaultBasemap       the default basemap for the user
 * @param defaultBasemapId     the default basemap id for the user
 * @param locale               the default locale for the user
 * @param email                the email for the user
 * @param emailOptIn           if the user wants to receive emails from MR (not us!)
 * @param leaderboardOptOut    if the user wants to opt out of the leaderboard
 * @param needsReview          if the user wants others to review their work by default
 * @param isReviewer           if the user is a reviewer
 * @param allowFollowing       if the user allows following
 * @param theme                the theme for the MR website
 * @param customBasemaps       the custom basemaps for the user
 * @param seeTagFixSuggestions if the user wants to see tag fix suggestions
 */
public record UserSettings(@Nullable Integer defaultEditor, @Nullable Integer defaultBasemap,
                           @Nullable String defaultBasemapId, @Nullable String locale,
                           @Nullable String email, @Nullable Boolean emailOptIn,
                           @Nullable Boolean leaderboardOptOut, @Nullable Integer needsReview,
                           @Nullable Boolean isReviewer, @Nullable Boolean allowFollowing,
                           @Nullable Integer theme, CustomBasemap[] customBasemaps,
                           @Nullable Boolean seeTagFixSuggestions) {
}
