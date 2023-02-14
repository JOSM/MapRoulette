// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import org.openstreetmap.josm.data.osm.OsmPrimitiveType;

/**
 * Create a new OSM object
 *
 * @param osmId   The osm id
 * @param osmType The osm type
 * @param fields  The fields for object
 * @param tags    The tags
 */
public record ElementCreate(long osmId, OsmPrimitiveType osmType, Object fields, Object tags) {
}
