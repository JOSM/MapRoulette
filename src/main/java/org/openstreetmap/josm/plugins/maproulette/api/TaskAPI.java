// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api;

import static org.openstreetmap.josm.plugins.maproulette.config.MapRouletteConfig.getBaseUrl;
import static org.openstreetmap.josm.plugins.maproulette.util.HttpClientUtils.put;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import jakarta.json.Json;
import javax.swing.text.html.Option;

import org.openstreetmap.josm.plugins.maproulette.api.enums.TaskStatus;
import org.openstreetmap.josm.plugins.maproulette.api.model.ClusteredPoint;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.plugins.maproulette.api.parsers.ClusteredPointParser;
import org.openstreetmap.josm.plugins.maproulette.api.parsers.TaskParser;
import org.openstreetmap.josm.plugins.maproulette.util.HttpClientUtils;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Utils;

/**
 * Task API
 */
public final class TaskAPI {
    /**
     * The basic task api endpoint
     */
    private static final String TASK = "/task";
    /**
     * The basic tasks api endpoint
     */
    private static final String TASKS = TASK + "s";

    /**
     * Don't allow instantiation of this utility class
     */
    private TaskAPI() {
        // Hide constructor
    }

    /**
     * Get tasks in a bounding box
     *
     * @param minLat            The minimum latitude for the bounding box (left)
     * @param minLon            The minimum longitude for the bounding box (bottom)
     * @param maxLat            The maximum latitude for the bounding box (right)
     * @param maxLon            The maximum longitude for the bounding box (top)
     * @param limit             Limit the number of results returned in the response
     * @param page              Used to paginate through the responses
     * @param excludeLocked     {@code true} will ignore the lock status of tasks
     * @param sort              Unknown
     * @param order             ASC/DESC
     * @param includeTotal      Include total
     * @param includeGeometries include the geometries
     * @param includeTags       include the tags
     * @return The collection of tasks
     * @throws IOException if there was a problem communicating with the server
     */
    public static ClusteredPoint[] box(double minLon, double minLat, double maxLon, double maxLat, int limit, int page,
            boolean excludeLocked, String sort, String order, boolean includeTotal, boolean includeGeometries,
            boolean includeTags) throws IOException {
        Map<String, String> query = new TreeMap<>();
        if (limit > 0) {
            query.put("limit", String.valueOf(limit));
        }
        if (page > 0) {
            query.put("page", String.valueOf(page));
        }
        if (excludeLocked) {
            query.put("excludeLocked", "true");
        }
        if (sort != null && !sort.isBlank()) {
            query.put("sort", sort);
        }
        if (order != null && !order.isBlank()) {
            query.put("order", order);
        }
        if (includeTotal) {
            query.put("includeTotal", "true");
        }
        if (includeGeometries) {
            query.put("includeGeometries", "true");
        }
        if (includeTags) {
            query.put("includeTags", "true");
        }
        final var client = put(getBaseUrl() + TASKS + "/box/" + minLon + "/" + minLat + "/" + maxLon + "/" + maxLat,
                query);
        try (var inputstream = client.connect().getContent()) {
            return (ClusteredPoint[]) ClusteredPointParser.parse(inputstream);
        } finally {
            client.disconnect();
        }
    }

    /**
     * Get a task without locking it
     *
     * @param task The task to get
     * @return The task for the id
     * @throws IOException if there was a problem communicating with the server
     */
    public static Task get(long task) throws IOException {
        final var client = HttpClientUtils.get(getBaseUrl() + TASK + "/" + task);
        try (var inputstream = client.connect().getContent()) {
            return (Task) TaskParser.parseTask(inputstream);
        } finally {
            client.disconnect();
        }
    }

    /**
     * Start and lock a task
     *
     * @param task The task to start
     * @return The updated task
     * @throws IOException if there was a problem communicating with the server
     * @throws UnauthorizedException If we aren't authorized for the server
     */
    public static Task start(long task) throws IOException {
        final var client = HttpClientUtils.get(getBaseUrl() + TASK + "/" + task + "/start");
        try (var inputstream = client.connect().getContent()) {
            return (Task) TaskParser.parseTask(inputstream);
        } finally {
            client.disconnect();
        }
    }

    /**
     * Release a tasks lock (the user must hold the lock)
     *
     * @param task The task to unlock
     * @return The unlocked task
     * @throws IOException if there was a problem communicating with the server
     */
    public static Task release(long task) throws IOException {
        final var client = HttpClientUtils.get(getBaseUrl() + TASK + "/" + task + "/release");
        try (var inputstream = client.connect().getContent()) {
            return (Task) TaskParser.parseTask(inputstream);
        } finally {
            client.disconnect();
        }
    }

    /**
     * Refresh an existing lock on a task
     *
     * @param task The task to update
     * @return The updated task
     * @throws IOException if there was a problem communicating with the server
     */
    public static Task refreshLock(long task) throws IOException {
        final var client = HttpClientUtils.get(getBaseUrl() + TASK + "/" + task + "/refreshLock");
        try (var inputstream = client.connect().getContent()) {
            return (Task) TaskParser.parseTask(inputstream);
        } finally {
            client.disconnect();
        }
    }

    /**
     * Update the changeset for a task
     *
     * @param task the task to update the changeset for
     * @return The updated task
     * @throws IOException if there was a problem communicating with the server
     */
    public static Task changeset(long task) throws IOException {
        final var client = put(getBaseUrl() + TASK + "/" + task + "/changeset", Collections.emptyMap());
        try (var inputstream = client.connect().getContent()) {
            return (Task) TaskParser.parseTask(inputstream);
        } finally {
            client.disconnect();
        }
    }

    /**
     * Update the status of a task
     *
     * @param task          The task to update
     * @param status        The status to set
     * @param comment       The comment to use
     * @param tags          The tags to use
     * @param requestReview Request review (or not), overrides user settings
     * @param completionResponses The completion responses
     * @return {@code true} if the task update was successful
     * @throws IOException if there was a problem communicating with the server
     */
    public static boolean updateStatus(long task, TaskStatus status, String comment, String tags, Boolean requestReview,
            Map<String, Option> completionResponses) throws IOException {
        Map<String, String> query = new TreeMap<>();
        if (comment != null && !comment.isBlank()) {
            query.put("comment", comment);
        }
        if (tags != null && !tags.isBlank()) {
            query.put("tags", tags);
        }
        if (requestReview != null) {
            query.put("requestReview", requestReview.toString());
        }
        final byte[] body;
        if (completionResponses != null) {
            var jsonBuilder = Json.createObjectBuilder();
            for (Map.Entry<String, Option> entry : completionResponses.entrySet()) {
                jsonBuilder.add(entry.getKey(), entry.getValue().getValue());
            }
            body = jsonBuilder.build().toString().getBytes(StandardCharsets.UTF_8);
        } else {
            body = null;
        }
        @SuppressWarnings("EnumOrdinal")
        final var client = put(getBaseUrl() + TASK + "/" + task + "/" + status.ordinal(), query, body);
        client.setHeader("content-type", "text/json");
        try {
            final var response = client.connect();
            final var content = response.fetchContent();
            if (!Utils.isStripEmpty(content)) {
                Logging.info(content);
            }
            return response.getResponseCode() == 204;
        } finally {
            client.disconnect();
        }
    }
}
