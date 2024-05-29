// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.util;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.openstreetmap.josm.plugins.maproulette.api.UnauthorizedException;
import org.openstreetmap.josm.tools.HttpClient;

/**
 * A utility class for making clients for use with the MapRoulette API
 */
public final class HttpClientUtils {
    /**
     * Prevent the utils class from being instantiated
     */
    private HttpClientUtils() {
        // Hide the constructor
    }

    /**
     * Get data
     *
     * @param url The url to GET
     * @return The client to use
     * @throws UnauthorizedException if the user hasn't logged in to MapRoulette
     */
    public static HttpClient get(String url) throws UnauthorizedException {
        return get(url, Collections.emptyMap());
    }

    /**
     * Get data
     *
     * @param url             The url to GET
     * @param queryParameters The query parameters
     * @return The client to use
     * @throws UnauthorizedException if the user hasn't logged in to MapRoulette
     */
    public static HttpClient get(String url, Map<String, String> queryParameters) throws UnauthorizedException {
        var client = HttpClient.create(safeUrl(url, queryParameters));
        sign(client);
        return client;
    }

    /**
     * Get the url in a safe manner
     *
     * @param url             The url to create
     * @param queryParameters The parameters to send
     * @return The URL
     */
    private static URL safeUrl(String url, Map<String, String> queryParameters) {
        try {
            if (queryParameters == null || queryParameters.isEmpty()) {
                return new URL(url);
            } else {
                return new URL(url + query(queryParameters));
            }
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Sign the client
     *
     * @param client The client to add the api key to
     * @throws UnauthorizedException if the user isn't logged in or hasn't logged in to MapRoulette before
     */
    private static void sign(HttpClient client) throws UnauthorizedException {
        client.setHeader("apiKey", OsmPreferenceUtils.getMapRouletteApiKey());
    }

    /**
     * Convert a map of parameters to a string
     *
     * @param queryParameters The query parameters to send the server
     * @return The parameters to send the server
     */
    private static String query(Map<String, String> queryParameters) {
        return queryParameters.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&", "?", ""));
    }

    /**
     * Put data
     *
     * @param url             The url to PUT
     * @param queryParameters The query parameters
     * @return The client to use
     * @throws UnauthorizedException if the user hasn't logged in to MapRoulette
     */
    public static HttpClient put(String url, Map<String, String> queryParameters) throws UnauthorizedException {
        var client = put(url, Collections.emptyMap(),
                query(queryParameters).substring(1).getBytes(StandardCharsets.UTF_8));
        client.setHeader("Content-Type", "application/x-www-form-urlencoded");
        return client;
    }

    /**
     * Put data
     *
     * @param url             The url to PUT
     * @param queryParameters The query parameters
     * @param body The body to use
     * @return The client to use
     * @throws UnauthorizedException if the user hasn't logged in to MapRoulette
     */
    public static HttpClient put(String url, Map<String, String> queryParameters, byte[] body)
            throws UnauthorizedException {
        var client = HttpClient.create(safeUrl(url, queryParameters), "PUT");
        client.setRequestBody(Objects.requireNonNullElse(body, new byte[0]));
        sign(client);
        return client;
    }

    /**
     * POST data
     *
     * @param url             The URL to POST
     * @param queryParameters The query parameters to be put in the body
     * @return The client to use
     * @throws UnauthorizedException if the user hasn't logged in to MapRoulette
     */
    public static HttpClient post(String url, Map<String, String> queryParameters) throws UnauthorizedException {
        var client = HttpClient.create(safeUrl(url, Collections.emptyMap()), "POST");
        client.setRequestBody(query(queryParameters).substring(1).getBytes(StandardCharsets.UTF_8));
        sign(client);
        return client;
    }

    /**
     * DELETE data
     *
     * @param url The URL to DELETE
     * @return The client to use
     * @throws UnauthorizedException if the user hasn't logged in to MapRoulette
     */
    public static HttpClient delete(String url) throws UnauthorizedException {
        var client = HttpClient.create(safeUrl(url, Collections.emptyMap()), "DELETE");
        sign(client);
        return client;
    }
}
