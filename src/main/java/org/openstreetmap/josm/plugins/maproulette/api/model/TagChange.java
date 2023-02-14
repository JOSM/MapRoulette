// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import java.util.Map;

import org.openstreetmap.josm.data.osm.OsmPrimitiveType;

/**
 * A tag change to do
 *
 * @param osmId   the id of the object
 * @param osmType the type of the object
 * @param updates The tags to update
 * @param deletes The keys to delete
 */
public record TagChange(long osmId, OsmPrimitiveType osmType, Map<String, String> updates,
                        String[] deletes) {
}
