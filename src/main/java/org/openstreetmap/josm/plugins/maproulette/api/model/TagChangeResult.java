// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import java.util.Map;

import org.openstreetmap.josm.data.osm.OsmPrimitiveType;

/**
 * The result of a change
 *
 * @param osmId   The osm id of the object
 * @param type    The primitive type
 * @param creates The tags that were created
 * @param updates The tags that were updated
 * @param deletes The tags that were deleted
 */
public record TagChangeResult(long osmId, OsmPrimitiveType type, Map<String, String> creates,
                              Map<String, String> updates, Map<String, String> deletes) {
}
