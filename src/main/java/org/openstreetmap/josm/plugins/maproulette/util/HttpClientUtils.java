// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.util;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

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
     */
    public static HttpClient get(String url) {
        return get(url, Collections.emptyMap());
    }

    /**
     * Get data
     *
     * @param url             The url to GET
     * @param queryParameters The query parameters
     * @return The client to use
     */
    public static HttpClient get(String url, Map<String, String> queryParameters) {
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
     */
    private static void sign(HttpClient client) {
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
     */
    public static HttpClient put(String url, Map<String, String> queryParameters) {
        var client = HttpClient.create(safeUrl(url, Collections.emptyMap()), "PUT");
        client.setRequestBody(query(queryParameters).substring(1).getBytes(StandardCharsets.UTF_8));
        sign(client);
        return client;
    }

    /**
     * POST data
     *
     * @param url             The URL to POST
     * @param queryParameters The query parameters to be put in the body
     * @return The client to use
     */
    public static HttpClient post(String url, Map<String, String> queryParameters) {
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
     */
    public static HttpClient delete(String url) {
        var client = HttpClient.create(safeUrl(url, Collections.emptyMap()), "DELETE");
        sign(client);
        return client;
    }
}
