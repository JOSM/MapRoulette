// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api;

import static org.openstreetmap.josm.plugins.maproulette.config.MapRouletteConfig.getBaseUrl;
import static org.openstreetmap.josm.plugins.maproulette.util.HttpClientUtils.delete;
import static org.openstreetmap.josm.plugins.maproulette.util.HttpClientUtils.get;
import static org.openstreetmap.josm.plugins.maproulette.util.HttpClientUtils.post;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.annotation.Nonnull;
import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;

import org.openstreetmap.josm.plugins.maproulette.api.model.TaskBundle;

/**
 * A class for methods related to the bundle apis
 */
public final class BundleAPI {
    /**
     * The base task bundle path
     */
    private static final String PATH = "/taskBundle";

    /**
     * Hide the constructor for this API object
     */
    private BundleAPI() {
        // Hide constructor
    }

    /**
     * Calls POST /taskBundle
     *
     * @param bundle The bundle to create
     * @return The created bundle. This is <i>not</i> the same as the bundle sent in.
     * @throws IOException if there was a problem communicating with the server
     */
    @Nonnull
    public static TaskBundle createBundle(TaskBundle bundle) throws IOException {
        final var client = post(getBaseUrl() + PATH, null); // fixme add body
        try {
            try (var inputStream = client.connect().getContent()) {
                return parseBundle(inputStream);
            }
        } finally {
            client.disconnect();
        }
    }

    /**
     * Parse a {@link TaskBundle} from an {@link InputStream}
     *
     * @param bundle The stream with the bundle information
     * @return The bundle of tasks
     */
    @Nonnull
    public static TaskBundle parseBundle(InputStream bundle) {
        try (JsonReader reader = Json.createReader(bundle)) {
            JsonStructure structure = reader.read();
            if (structure.getValueType() == JsonValue.ValueType.OBJECT) {
                var obj = structure.asJsonObject();
                return new TaskBundle(obj.getJsonNumber("id").longValue(), obj.getString("name"),
                        obj.getJsonNumber("ownerId").longValue(),
                        obj.getJsonArray("taskIds").stream().mapToLong(val -> ((JsonNumber) val).longValue()).toArray(),
                        null /* TODO parse tasks? */);
            } else {
                throw new IllegalArgumentException(structure.toString());
            }
        }
    }

    /**
     * Get a specified task bundle
     *
     * @param id The id of the bundle to get
     * @return The specified bundle
     */
    @Nonnull
    public static TaskBundle getBundle(long id) {
        final var client = get(getBaseUrl() + PATH + "/" + id);
        try {
            try (var inputStream = client.connect().getContent()) {
                return parseBundle(inputStream);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            client.disconnect();
        }
    }

    /**
     * Delete a bundle
     *
     * @param id The bundle to delete
     * @return {@code true} if the deletion was successful
     */
    public static boolean deleteBundle(long id) {
        final var client = delete(getBaseUrl() + PATH + "/" + id);
        try {
            int responseCode = client.connect().getResponseCode();
            if (responseCode == 401) {
                throw new UnauthorizedException(client.getURL().toExternalForm());
            }
            return responseCode == 200;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            client.disconnect();
        }
    }

    /**
     * Unbundle the specified tasks
     *
     * @param original The original bundle
     * @param taskIds  The tasks to remove
     * @return The new bundle
     */
    @Nonnull
    public static TaskBundle unbundle(TaskBundle original, long... taskIds) {
        final var client = get(getBaseUrl() + PATH + "/" + original.id() + "/unbundle",
                Map.of("taskIds", LongStream.of(taskIds).mapToObj(Long::toString).collect(Collectors.joining(","))));
        try {
            try (var inputstream = client.connect().getContent()) {
                return parseBundle(inputstream);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            client.disconnect();
        }
    }
}
