// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.parsers;

import javax.annotation.Nullable;
import jakarta.json.JsonObject;

import org.openstreetmap.josm.plugins.maproulette.api.model.Point;

/**
 * A parser for points
 */
public final class PointParser {
    /**
     * Prevent instantiation of the parser
     */
    private PointParser() {
        // Hide constructor
    }

    /**
     * Parse a point from a JsonObject
     *
     * @param object The object with lat and lng values
     * @return The parsed point
     */
    @Nullable
    public static Point parse(JsonObject object) {
        if (object.containsKey("lat") && object.containsKey("lng")) {
            return new Point(object.getJsonNumber("lat").doubleValue(), object.getJsonNumber("lng").doubleValue());
        } else if (object.containsKey("type") && "Point".equals(object.getString("type"))) {
            final var coordinates = object.getJsonArray("coordinates");
            return new Point(coordinates.getJsonNumber(1).doubleValue(), coordinates.getJsonNumber(0).doubleValue());
        }
        return null;
    }
}
