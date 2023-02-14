// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.parsers;

import static org.openstreetmap.josm.plugins.maproulette.api.parsers.ParsingUtils.optionalInstant;
import static org.openstreetmap.josm.plugins.maproulette.api.parsers.ParsingUtils.optionalInteger;
import static org.openstreetmap.josm.plugins.maproulette.api.parsers.ParsingUtils.optionalObject;

import javax.json.JsonNumber;
import javax.json.JsonObject;

import org.openstreetmap.josm.plugins.maproulette.api.model.PointReview;

/**
 * A parser for {@link org.openstreetmap.josm.plugins.maproulette.api.model.PointReview}
 */
public final class PointReviewParser {
    /**
     * An empty long array to avoid many empty long array creations
     */
    private static final long[] EMPTY_LONG = new long[0];

    /**
     * Don't allow users to instantiate this object
     */
    private PointReviewParser() {
        // Hide constructor
    }

    /**
     * Parse a {@link PointReview} object
     *
     * @param object The object to parse
     * @return The parsed review object
     */
    static PointReview parse(JsonObject object) {
        return new PointReview(optionalInteger(object, "reviewStatus"),
                optionalObject(object, "reviewRequestedBy", UserParser::parse),
                optionalObject(object, "reviewedBy", UserParser::parse), optionalInstant(object, "reviewedAt"),
                optionalInteger(object, "metaReviewStatus"),
                optionalObject(object, "metaReviewedBy", UserParser::parse), optionalInstant(object, "metaReviewedAt"),
                optionalInstant(object, "reviewStartedAt"),
                object.containsKey("additionalReviewers")
                        ? object.getJsonArray("additionalReviewers").getValuesAs(JsonNumber.class).stream()
                                .mapToLong(JsonNumber::longValue).toArray()
                        : EMPTY_LONG);
    }
}
