// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.parsers;

import static org.openstreetmap.josm.plugins.maproulette.api.parsers.ParsingUtils.optionalInstant;
import static org.openstreetmap.josm.plugins.maproulette.api.parsers.ParsingUtils.optionalLong;
import static org.openstreetmap.josm.plugins.maproulette.api.parsers.ParsingUtils.optionalObject;

import java.io.InputStream;
import java.time.Instant;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

import org.openstreetmap.josm.plugins.maproulette.api.enums.Difficulty;
import org.openstreetmap.josm.plugins.maproulette.api.enums.TaskStatus;
import org.openstreetmap.josm.plugins.maproulette.api.model.ClusteredPoint;

/**
 * Parser for {@link org.openstreetmap.josm.plugins.maproulette.api.model.ClusteredPoint}
 */
public final class ClusteredPointParser {
    /**
     * Prevent instantiation of this parser
     */
    private ClusteredPointParser() {
        // Hide constructor
    }

    /**
     * Parse {@link org.openstreetmap.josm.plugins.maproulette.api.model.ClusteredPoint}s from an {@link InputStream}
     *
     * @param inputStream The originating stream
     * @return The {@link org.openstreetmap.josm.plugins.maproulette.api.model.ClusteredPoint}(s). Either an array or a single point.
     */
    public static Object parse(InputStream inputStream) {
        try (JsonParser parser = Json.createParser(inputStream)) {
            while (parser.hasNext()) {
                switch (parser.next()) {
                case START_ARRAY:
                    return parser.getArrayStream().map(ClusteredPointParser::parse).filter(Objects::nonNull)
                            .toArray(ClusteredPoint[]::new);
                case START_OBJECT:
                    return parse(parser.getObject());
                default:
                    // Keep going
                }
            }
        }
        throw new IllegalArgumentException("InputStream did not contain a valid ClusteredPoint");
    }

    /**
     * Create a new point from a value
     *
     * @param value The value object
     * @return The point
     */
    @Nullable
    private static ClusteredPoint parse(JsonValue value) {
        if (value instanceof JsonObject object) {
            return new ClusteredPoint(object.getJsonNumber("id").longValue(), object.getJsonNumber("owner").longValue(),
                    object.getString("ownerName"), object.getString("title"),
                    object.getJsonNumber("parentId").longValue(), object.getString("parentName"),
                    Objects.requireNonNull(PointParser.parse(object.getJsonObject("point"))),
                    object.getString("bounding"), object.getString("blurb"),
                    Instant.parse(object.getString("modified")),
                    object.containsKey("difficulty") && object.getInt("difficulty") > 0
                            ? Difficulty.values()[object.getInt("difficulty") - 1]
                            : null,
                    object.getInt("type"),
                    object.containsKey("status") ? TaskStatus.values()[object.getInt("status")] : null,
                    object.getString("cooperativeWork", null), optionalInstant(object, "mappedOn"),
                    optionalLong(object, "completedTimeSpent"),
                    optionalObject(object, "completedBy", UserParser::parse),
                    PointReviewParser.parse(object.getJsonObject("pointReview")), object.getInt("priority", 0),
                    optionalLong(object, "bundleId"), object.getBoolean("isBundlePrimary", false));
        }
        return null;
    }
}
