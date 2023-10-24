// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import java.util.Map;

import jakarta.annotation.Nonnull;

/**
 * The tags to update or delete
 *
 * @param updates The tags to change or create
 * @param deletes The keys to delete
 */
public record ElementTagChange(@Nonnull Map<String, String> updates, @Nonnull String[] deletes) {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ElementTagChange other && RecordUtils.equals(this, other);
    }

    @Override
    public int hashCode() {
        return RecordUtils.hashCode(this);
    }

    @Override
    public String toString() {
        return RecordUtils.toString(this);
    }
}
