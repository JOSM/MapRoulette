// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.parsers;

import javax.annotation.Nullable;
import javax.json.JsonObject;

import org.openstreetmap.josm.plugins.maproulette.api.model.PointReview;
import org.openstreetmap.josm.plugins.maproulette.api.model.PublicUser;

/**
 * A parser for user objects
 */
final class UserParser {
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
