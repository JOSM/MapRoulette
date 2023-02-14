// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

/**
 * A change element for cooperative challenges
 *
 * @param creates Created elements
 * @param updates Updated elements (may include deletions)
 */
public record OSMChange(ElementCreate[] creates, ElementUpdate[] updates) {
}
