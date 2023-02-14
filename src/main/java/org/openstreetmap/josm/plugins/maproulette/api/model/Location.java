// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import javax.annotation.Nullable;

/**
 * A location
 *
 * @param latitude  The latitude
 * @param longitude The longitude
 * @param name      The name of the location
 */
public record Location(double latitude, double longitude, @Nullable String name) {
}
