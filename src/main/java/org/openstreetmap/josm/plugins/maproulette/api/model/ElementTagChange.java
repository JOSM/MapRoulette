// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * The tags to update or delete
 *
 * @param updates The tags to change or create
 * @param deletes The keys to delete
 */
public record ElementTagChange(@Nonnull Map<String, String> updates, @Nonnull String[] deletes) {
}
