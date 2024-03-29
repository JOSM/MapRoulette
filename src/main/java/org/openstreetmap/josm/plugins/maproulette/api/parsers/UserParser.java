// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.parsers;

import org.openstreetmap.josm.plugins.maproulette.api.model.PointReview;
import org.openstreetmap.josm.plugins.maproulette.api.model.PublicUser;

import jakarta.annotation.Nullable;
import jakarta.json.JsonObject;

/**
 * A parser for user objects
 */
final class UserParser {
    private UserParser() {
        /* Hide constructor */ }

    /**
     * Parse a {@link PointReview} object
     *
     * @param object The object to parse
     * @return The parsed review object
     */
    @Nullable
    static PublicUser parse(@Nullable JsonObject object) {
        if (object != null && object.containsKey("username") && object.containsKey("id")) {
            return new PublicUser(object.getJsonNumber("id").longValue(), null, object.getString("username"), null,
                    null);
        }
        return null;
    }
}
