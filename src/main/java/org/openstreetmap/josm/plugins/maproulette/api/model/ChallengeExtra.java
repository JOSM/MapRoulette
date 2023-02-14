// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import java.time.Instant;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Extra information for a challenge
 *
 * @param defaultZoom          The default zoom
 * @param minZoom              The min zoom
 * @param maxZoom              The max zoom
 * @param defaultBasemap       The default basemap
 * @param defaultBasemapId     The default basemap id
 * @param customBasemap        The custom basemap
 * @param updateTasks          {@code true} to update task (only if updating a challenge)
 * @param exportableProperties The properties to export
 * @param osmIdProperty        The property for the OSM id
 * @param preferredTags        The preferred tags
 * @param preferredReviewTags  The preferred tags for review
 * @param limitTags            only allow {@link #preferredTags()}
 * @param limitReviewTags      only allow {@link #preferredReviewTags()}
 * @param taskStyles           The task styles
 * @param taskBundleIdProperty The task bundle id property
 * @param isArchived           {@code true} if the challenge is archived
 * @param systemArchivedAt     The time that the challenge was archived
 * @param presets              Presets to allow (iD)
 */
public record ChallengeExtra(int defaultZoom, int minZoom, int maxZoom, int defaultBasemap,
                             String defaultBasemapId, String customBasemap, boolean updateTasks,
                             String exportableProperties, String osmIdProperty, String preferredTags,
                             String preferredReviewTags, boolean limitTags, boolean limitReviewTags,
                             String taskStyles, String taskBundleIdProperty, boolean isArchived,
                             Instant systemArchivedAt, @Nullable String... presets) {
    /**
     * The default OSM id properties, as defined by
     * <a href="https://learn.maproulette.org/documentation/setting-external-task-identifiers/">MapRoulette documentation</a>.
     * There are some additional identifiers from
     * <a href="https://github.com/maproulette/maproulette3/blob/develop/src/interactions/TaskFeature/AsIdentifiableFeature.js">
     * AsIdentifiableFeatures.js
     * </a>
     */
    public static final List<String> DEFAULT_OSM_ID_PROPERTIES = List.of("id", "@id", "osmid", "osmIdentifier", "osm_id", "name");
}
