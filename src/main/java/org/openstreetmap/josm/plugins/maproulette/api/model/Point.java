// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import org.openstreetmap.josm.data.IQuadBucketType;
import org.openstreetmap.josm.data.coor.ILatLon;
import org.openstreetmap.josm.data.osm.BBox;

/**
 * A point on on a map
 *
 * @param lat The latitude
 * @param lon The longitude
 */
public record Point(double lat, double lon) implements ILatLon, IQuadBucketType, AbstractNode {
    @Override
    public BBox getBBox() {
        return new BBox(this);
    }
}
