// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

/**
 * Submit tag changes
 *
 * @param comment The comment to use
 * @param changes The changes to do
 */
public record TagChangeSubmission(String comment, TagChange[] changes) {
}
