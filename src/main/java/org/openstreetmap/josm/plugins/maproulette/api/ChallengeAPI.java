// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api;

import static org.openstreetmap.josm.plugins.maproulette.api.parsers.ParsingUtils.optionalArray;
import static org.openstreetmap.josm.plugins.maproulette.api.parsers.ParsingUtils.optionalInstant;
import static org.openstreetmap.josm.plugins.maproulette.api.parsers.ParsingUtils.optionalInteger;
import static org.openstreetmap.josm.plugins.maproulette.api.parsers.ParsingUtils.optionalObject;
import static org.openstreetmap.josm.plugins.maproulette.config.MapRouletteConfig.getBaseUrl;
import static org.openstreetmap.josm.plugins.maproulette.util.HttpClientUtils.get;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

import org.openstreetmap.josm.plugins.maproulette.api.enums.Priority;
import org.openstreetmap.josm.plugins.maproulette.api.model.Challenge;
import org.openstreetmap.josm.plugins.maproulette.api.model.ChallengeCreation;
import org.openstreetmap.josm.plugins.maproulette.api.model.ChallengeExtra;
import org.openstreetmap.josm.plugins.maproulette.api.model.ChallengeGeneral;
import org.openstreetmap.josm.plugins.maproulette.api.model.ChallengePriority;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.plugins.maproulette.api.parsers.PointParser;
import org.openstreetmap.josm.plugins.maproulette.api.parsers.TaskParser;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.json.Json;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

/**
 * A class for methods related to the challenge apis
 */
public final class ChallengeAPI {
    /**
     * The base path for challenge requests
     */
    private static final String PATH = "/challenge";

    /**
     * Don't allow the API object to be instantiated
     */
    private ChallengeAPI() {
        // Hide constructor
    }

    /**
     * Create a challenge
     *
     * @param original The original challenge
     * @return The challenge from the server
     */
    public static Challenge createChallenge(Challenge original) {
        // POST /challenge
        throw new UnsupportedOperationException();
    }

    /**
     * Update the Challenge archive status
     *
     * @param id         The challenge to update
     * @param isArchived The new archive status
     */
    public static void updateChallengeArchive(long id, boolean isArchived) {
        // POST /challenge/id/archive with the boolean as the body
        throw new UnsupportedOperationException();
    }

    /**
     * Get the next task (by task ordering). This does loop to the first task if this is the last task.
     *
     * @param challengeId The challenge to get tasks for
     * @param currentTask The current task
     * @param status      The task status to limit the response by
     * @return The next task
     * @throws IOException if there was a problem communicating with the server
     */
    @Nonnull
    public static Task nextTask(long challengeId, long currentTask, String... status) throws IOException {
        final var client = get(getBaseUrl() + PATH + "/" + challengeId + "/nextTask/" + currentTask,
                status.length > 0 ? Map.of("statusList", String.join(",", status)) : null);
        try {
            try (var inputStream = client.connect().getContent()) {
                return (Task) TaskParser.parseTask(inputStream);
            }
        } finally {
            client.disconnect();
        }
    }

    /**
     * Get the previous task (by task ordering). This does loop to the first task if this is the last task.
     *
     * @param challengeId The challenge to get tasks for
     * @param currentTask The current task
     * @param status      The task status to limit the response by
     * @return The previous task
     * @throws IOException if there was a problem communicating with the server
     */
    @Nonnull
    public static Task previousTask(long challengeId, long currentTask, String... status) throws IOException {
        final var client = get(getBaseUrl() + PATH + "/" + challengeId + "/previousTask/" + currentTask,
                status.length > 0 ? Map.of("statusList", String.join(",", status)) : null);
        try {
            try (var inputStream = client.connect().getContent()) {
                return (Task) TaskParser.parseTask(inputStream);
            }
        } finally {
            client.disconnect();
        }
    }

    /**
     * Get a random task (prioritized).
     *
     * @param challengeId  The challenge to get tasks for
     * @param searchString The string to search for (case insensitive)
     * @param tags         The task status to limit the response by
     * @param limit        The number of prioritized tasks to get. If less than zero, one is used.
     * @param proximity    The current task
     * @return The next task
     * @throws IOException if there was a problem communicating with the server
     */
    @Nonnull
    public static Task[] prioritizedTask(long challengeId, @Nullable String searchString, @Nullable String[] tags,
            int limit, long proximity) throws IOException {
        return taskCollectionEndpoints("/tasks/prioritizedTasks", challengeId, searchString, tags, limit, proximity);
    }

    /**
     * Common method for task collection endpoints
     *
     * @param challengeId  The challenge to get tasks for
     * @param searchString The string to search for (case insensitive)
     * @param tags         The task status to limit the response by
     * @param limit        The number of prioritized tasks to get. If less than zero, one is used.
     * @param proximity    The current task
     * @return The next task
     * @throws IOException if there was a problem communicating with the server
     */
    private static Task[] taskCollectionEndpoints(@Nonnull String path, long challengeId, @Nullable String searchString,
            @Nullable String[] tags, int limit, long proximity) throws IOException {
        Map<String, String> query = new TreeMap<>();
        if (searchString != null && !searchString.isBlank()) {
            query.put("s", searchString);
        }
        if (tags != null && tags.length > 0) {
            query.put("tags", String.join(",", tags));
        }
        if (limit > 0) {
            query.put("limit", String.valueOf(limit));
        }
        if (proximity > 0) {
            query.put("proximity", String.valueOf(proximity));
        }
        final var client = get(getBaseUrl() + PATH + "/" + challengeId + path, query);
        try {
            try (var inputStream = client.connect().getContent()) {
                return (Task[]) TaskParser.parseTask(inputStream);
            }
        } finally {
            client.disconnect();
        }
    }

    /**
     * Retrieve random tasks
     *
     * @param challengeId  The challenge to get tasks for
     * @param searchString The string to search for (case insensitive)
     * @param tags         The task status to limit the response by
     * @param limit        The number of prioritized tasks to get. If less than zero, one is used.
     * @param proximity    The current task
     * @return The next task
     * @throws IOException if there was a problem communicating with the server
     */
    public static Task[] randomTask(long challengeId, @Nullable String searchString, @Nullable String[] tags, int limit,
            long proximity) throws IOException {
        return taskCollectionEndpoints("/tasks/prioritizedTasks", challengeId, searchString, tags, limit, proximity);
    }

    /**
     * Get tasks near the specified task with the same challenge
     *
     * @param challengeId       The challenge id
     * @param proximityId       The proximity task id
     * @param excludeSelfLocked Exclude tasks the user has locked (default should be {@code false})
     * @param limit             Limit the number of results in the response. Default should be {@code 5}.
     * @param proximity         id of task around which geographically closest tasks are desired
     *                          (note: this seems like it might be a bug in the API)
     * @return The tasks
     * @throws IOException if there was a problem communicating with the server
     */
    public static Task[] tasksNearby(long challengeId, long proximityId, boolean excludeSelfLocked, int limit,
            long proximity) throws IOException {
        Map<String, String> query = new TreeMap<>();
        if (!excludeSelfLocked) {
            query.put("excludeSelfLocked", "true");
        }
        if (limit > 0) {
            query.put("limit", String.valueOf(limit));
        }
        if (proximity > 0) {
            query.put("proximity", String.valueOf(proximity));
        }
        final var client = get(getBaseUrl() + PATH + "/" + challengeId + "/tasksNearby/" + proximityId, query);
        try {
            try (var inputStream = client.connect().getContent()) {
                return (Task[]) TaskParser.parseTask(inputStream);
            }
        } finally {
            client.disconnect();
        }
    }

    /**
     * Get a specified challenge
     *
     * @param challengeId The challenge to get
     * @return The challenge
     * @throws IOException if there was a problem communicating with the server
     */
    public static Challenge challenge(long challengeId) throws IOException {
        final var client = get(getBaseUrl() + PATH + "/" + challengeId, null);
        try {
            try (var inputstream = client.connect().getContent()) {
                return parseChallenge(inputstream);
            }
        } finally {
            client.disconnect();
        }
    }

    /**
     * Parse a challenge
     *
     * @param inputStream The incoming stream
     * @return The challenge
     */
    @Nonnull
    private static Challenge parseChallenge(InputStream inputStream) {
        try (var parser = Json.createReader(inputStream)) {
            JsonStructure structure = parser.read();
            if (structure.getValueType() == JsonValue.ValueType.OBJECT) {
                var obj = structure.asJsonObject();
                // These may be flattened into the top-level challenge object after creation.
                final var challengeGeneral = optionalObject(obj, "general", ChallengeAPI::parseChallengeGeneral);
                final var challengePriority = optionalObject(obj, "priority", ChallengeAPI::parseChallengePriority);
                final var challengeCreation = optionalObject(obj, "creation", ChallengeAPI::parseChallengeCreation);
                final var challengeExtra = optionalObject(obj, "extra", ChallengeAPI::parseChallengeExtra);
                return new Challenge(obj.getJsonNumber("id").longValue(), obj.getString("name"),
                        Instant.parse(obj.getString("created")), Instant.parse(obj.getString("modified")),
                        obj.getString("description", null), obj.getBoolean("deleted"), obj.getString("infoLink", null),
                        challengeGeneral == null ? ChallengeAPI.parseChallengeGeneral(obj) : challengeGeneral,
                        challengeCreation == null ? ChallengeAPI.parseChallengeCreation(obj) : challengeCreation,
                        challengePriority == null ? ChallengeAPI.parseChallengePriority(obj) : challengePriority,
                        challengeExtra == null ? ChallengeAPI.parseChallengeExtra(obj) : challengeExtra,
                        optionalInteger(obj, "status"), obj.getString("statusMessage", null),
                        optionalInstant(obj, "lastTaskRefresh"), optionalInstant(obj, "dataOriginDate"),
                        optionalObject(obj, "location", PointParser::parse),
                        optionalObject(obj, "bounding", Object::toString), optionalInteger(obj, "completionPercentage"),
                        optionalInteger(obj, "tasksRemaining"));
            } else {
                throw new IllegalArgumentException("Bad challenge json");
            }
        }
    }

    /**
     * Parse a general info object for a challenge
     *
     * @param object The json value
     * @return The object
     */
    @Nonnull
    private static ChallengeGeneral parseChallengeGeneral(JsonObject object) {
        return new ChallengeGeneral(object.getJsonNumber("owner").longValue(),
                object.getJsonNumber("parent").longValue(), object.getString("instruction"),
                object.getInt("difficulty"), object.getString("blurb", null), object.getBoolean("enabled"),
                object.getBoolean("featured"), object.getInt("cooperativeType"), object.getInt("popularity"),
                object.getString("checkinComment"), object.getString("checkinSource"),
                object.getBoolean("changesetUrl", false), optionalArray(object, "virtualParents", array -> array
                        .getValuesAs(JsonNumber.class).stream().mapToLong(JsonNumber::longValue).toArray()),
                object.getBoolean("requiresLocal"));
    }

    /**
     * Parse the priority rules
     *
     * @param object The object to parse
     * @return The priority rules
     */
    @Nonnull
    private static ChallengePriority parseChallengePriority(JsonObject object) {
        return new ChallengePriority(
                object.containsKey("defaultPriority") ? Priority.values()[object.getInt("defaultPriority")]
                        : Priority.MEDIUM,
                object.get("highPriorityRule").toString(), object.get("mediumPriorityRule").toString(),
                object.get("lowPriorityRule").toString());
    }

    /**
     * Parse the challenge creation object
     *
     * @param object the object to parse
     * @return The parsed object
     */
    @Nonnull
    private static ChallengeCreation parseChallengeCreation(JsonObject object) {
        return new ChallengeCreation(object.getString("overpassQL", null), object.getString("remoteGeoJson", null),
                object.getString("overpassTargetType", null));
    }

    /**
     * Parse extra information
     *
     * @param object The object to parse
     * @return The parsed information
     */
    @Nonnull
    private static ChallengeExtra parseChallengeExtra(JsonObject object) {
        return new ChallengeExtra(object.getInt("defaultZoom"), object.getInt("minZoom"), object.getInt("maxZoom"),
                object.getInt("defaultBasemap", -1), object.getString("defaultBasemapId", null),
                object.getString("customBasemap", null), object.getBoolean("updateTasks", false),
                object.getString("exportableProperties", null), object.getString("osmIdProperty", null),
                object.getString("preferredTags", null), object.getString("preferredReviewTags", null),
                object.getBoolean("limitTags"), object.getBoolean("limitReviewTags"),
                optionalArray(object, "taskStyles", JsonValue::toString),
                object.getString("taskBundleIdProperty", null), object.getBoolean("isArchived"),
                optionalInstant(object, "systemArchivedAt"), optionalArray(object, "presets", array -> array
                        .getValuesAs(JsonString.class).stream().map(JsonString::getString).toArray(String[]::new)));
    }
}
