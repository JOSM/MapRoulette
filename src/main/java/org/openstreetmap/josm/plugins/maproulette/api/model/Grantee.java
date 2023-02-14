// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

/**
 * A grantee
 *
 * @param granteeType The type of grantee
 * @param granteeId   The id of the grantee
 */
public record Grantee(int granteeType, long granteeId) {
}
