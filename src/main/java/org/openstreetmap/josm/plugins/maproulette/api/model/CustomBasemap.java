// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

/**
 * Custom basemap information
 *
 * @param id      The id for the basemap
 * @param name    The name of the basemap
 * @param url     The url for the basemap
 * @param overlay {@code true} if the basemap is actually an overlay
 */
public record CustomBasemap(long id, String name, String url, boolean overlay) implements Identifier {
}
