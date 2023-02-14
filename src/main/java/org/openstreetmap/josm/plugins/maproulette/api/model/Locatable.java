// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import org.openstreetmap.josm.data.IQuadBucketType;
import org.openstreetmap.josm.data.osm.BBox;

/**
 * Used for locatable objects
 */
public interface Locatable extends Identifier, IQuadBucketType {
    @Override
    default BBox getBBox() {
        if (this.location() != null) {
            return this.location().getBBox();
        }
        return new BBox();
    }

    /**
     * The location of this object
     *
     * @return The location
     */
    Point location();
}
