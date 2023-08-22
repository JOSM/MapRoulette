// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.parsers;

import jakarta.json.JsonObject;

import org.openstreetmap.josm.plugins.maproulette.api.UnauthorizedException;

/**
 * A parser for message objects. All methods can throw an {@link UnauthorizedException}!
 */
final class MessageParser {
    private MessageParser() {
        // Hide constructor
    }

    /**
     * Parse a potential API message
     *
     * @param jsonObject The object to look at
     * @throws UnauthorizedException If there is a problem
     */
    static void parse(JsonObject jsonObject) throws UnauthorizedException {
        if (jsonObject.containsKey("status") && jsonObject.containsKey("message")) {
            throw new UnauthorizedException(jsonObject.getString("message"));
        }
    }
}
