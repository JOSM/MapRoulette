// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import org.openstreetmap.josm.data.osm.OsmPrimitiveType;

/**
 * An element update
 *
 * @param osmId   The osm id to update
 * @param osmType The osm type of the object
 * @param version The expected version of the object
 * @param tags    The tag change
 */
public record ElementUpdate(long osmId, OsmPrimitiveType osmType, int version, ElementTagChange tags) {
}
