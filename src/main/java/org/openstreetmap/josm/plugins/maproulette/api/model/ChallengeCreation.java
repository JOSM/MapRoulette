// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

/**
 * Information for creating a challenge
 *
 * @param overpassQL         The overpass QL to use
 * @param remoteGeoJson      The remote geojson to use
 * @param overpassTargetType The overpass target type (to avoid having to deal with every node of a way being a task)
 */
public record ChallengeCreation(String overpassQL, String remoteGeoJson, String overpassTargetType) {
}
