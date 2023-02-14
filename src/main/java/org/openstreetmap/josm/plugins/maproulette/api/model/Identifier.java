// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

/**
 * An interface for records that have an id
 */
public interface Identifier {
    /**
     * Get the unique id for this object
     *
     * @return the unique id
     */
    long id();
}
