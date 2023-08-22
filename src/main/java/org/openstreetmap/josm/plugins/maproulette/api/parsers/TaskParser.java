// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.parsers;

import static org.openstreetmap.josm.plugins.maproulette.api.parsers.ParsingUtils.optionalInstant;
import static org.openstreetmap.josm.plugins.maproulette.api.parsers.ParsingUtils.optionalInteger;
import static org.openstreetmap.josm.plugins.maproulette.api.parsers.ParsingUtils.optionalLong;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;

import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmChangeReader;
import org.openstreetmap.josm.plugins.maproulette.api.enums.TaskStatus;
import org.openstreetmap.josm.plugins.maproulette.api.model.ElementCreate;
import org.openstreetmap.josm.plugins.maproulette.api.model.ElementTagChange;
import org.openstreetmap.josm.plugins.maproulette.api.model.ElementUpdate;
import org.openstreetmap.josm.plugins.maproulette.api.model.MapillaryImages;
import org.openstreetmap.josm.plugins.maproulette.api.model.OSMChange;
import org.openstreetmap.josm.plugins.maproulette.api.model.Point;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.plugins.maproulette.api.model.TaskReviewFields;
import org.openstreetmap.josm.tools.ExceptionUtil;
import org.openstreetmap.josm.tools.JosmRuntimeException;

/**
 * Parse tasks
 */
public final class TaskParser {
    /**
     * An empty long to avoid duplicate empty arrays
     */
    private static final long[] EMPTY_LONG = new long[0];

    /**
     * Don't allow instantiation of this parser
     */
    private TaskParser() {
        // Hide constructor
    }

    /**
     * Parse a task
     *
     * @param inputStream the stream to get the task from
     * @return The new task. May be a singular task or an array of tasks.
     */
    @Nonnull
    public static Object parseTask(InputStream inputStream) {
        try (var reader = Json.createParser(inputStream)) {
            while (reader.hasNext()) {
                var value = switch (reader.next()) {
                case START_OBJECT -> parseTask(reader.getObject());
                case START_ARRAY -> reader.getArrayStream().filter(JsonObject.class::isInstance)
                        .map(JsonObject.class::cast).map(TaskParser::parseTask).toArray(Task[]::new);
                default -> null;
                };
                if (value != null) {
                    return value;
                }
            }
        }
        throw new IllegalArgumentException("InputStream did not contain expected JSON data");
    }

    /**
     * Parse a task
     *
     * @param obj the JsonObject to get the task from
     * @return The new task
     */
    @Nonnull
    private static Task parseTask(JsonObject obj) {
        MessageParser.parse(obj);
        try {
            return new Task(obj.getJsonNumber("id").longValue(), obj.getString("name"),
                    Instant.parse(obj.getString("created")), Instant.parse(obj.getString("modified")),
                    obj.getJsonNumber("parent").longValue(), obj.getString("instruction"),
                    parseLocation(obj.getJsonObject("location")),
                    GeometryParser.parse(obj.getJsonObject("geometries").toString()),
                    obj.containsKey("cooperativeWork") ? parseCooperativeWork(obj.getJsonObject("cooperativeWork"))
                            : null,
                    TaskStatus.values()[obj.getInt("status")], optionalInstant(obj, "mappedOn"),
                    optionalLong(obj, "completedTimeSpent"), optionalLong(obj, "completedBy"),
                    parseTaskReviewFields(obj.getJsonObject("review")), obj.getInt("priority"),
                    optionalLong(obj, "changesetId"), obj.getString("completionResponses", null),
                    optionalLong(obj, "bundleId"), obj.getBoolean("isBundlePrimary", false),
                    obj.containsKey("mapillaryImages") ? parseMapillaryImages(obj.getJsonArray("mapillaryImages"))
                            : null,
                    obj.getString("errorTags"));
        } catch (IllegalDataException e) {
            throw new JosmRuntimeException(e);
        }
    }

    /**
     * Parse a location
     *
     * @param jsonObject The object with the location
     * @return The parsed location
     */
    @Nonnull
    private static Point parseLocation(JsonObject jsonObject) {
        if (jsonObject.containsKey("type") && "Point".equals(jsonObject.getString("type"))) {
            double[] location = jsonObject.getJsonArray("coordinates").getValuesAs(JsonNumber.class).stream()
                    .mapToDouble(JsonNumber::doubleValue).toArray();
            return new Point(location[1], location[0]);
        } else {
            throw new IllegalArgumentException("Unknown type: " + jsonObject);
        }
    }

    /**
     * Parse a cooperative work object
     *
     * @param object The object to parse
     * @return The changes to make to OSM
     */
    @Nullable
    private static Object parseCooperativeWork(JsonObject object) {
        final var meta = object.getJsonObject("meta");
        final var metaType = meta.getInt("type", -1);
        final var metaVersion = meta.getInt("version", -1);
        if (metaType == 1 && metaVersion == 2) {
            final var modifies = new ArrayList<ElementUpdate>();
            final var operations = object.getJsonArray("operations");
            for (var operation : operations.getValuesAs(JsonObject.class)) {
                final var type = operation.getString("operationType");
                if ("modifyElement".equals(type)) {
                    final var data = operation.getJsonObject("data");
                    final var id = parseId(data.getJsonString("id").getString());
                    final var elemOperations = data.getJsonArray("operations");
                    for (var elemOp : elemOperations.getValuesAs(JsonObject.class)) {
                        final var elemType = elemOp.getString("operation");
                        if ("setTags".equals(elemType)) {
                            final var elemData = elemOp.getJsonObject("data").entrySet().stream()
                                    .filter(entry -> entry.getValue() instanceof JsonString).collect(Collectors.toMap(
                                            Map.Entry::getKey, entry -> ((JsonString) entry.getValue()).getString()));
                            modifies.add(new ElementUpdate(id.getUniqueId(), id.getType(), Integer.MIN_VALUE,
                                    new ElementTagChange(elemData, new String[0])));
                        } else {
                            throw new IllegalArgumentException(object.toString());
                        }
                    }
                } else {
                    throw new IllegalArgumentException(object.toString());
                }
            }
            return new OSMChange(new ElementCreate[0], modifies.toArray(new ElementUpdate[0]));
        } else if (metaType == 2 && metaVersion == 2) {
            final var file = object.getJsonObject("file");
            if ("xml".equals(file.getString("type", null)) && "osc".equals(file.getString("format", null))
                    && "base64".equals(file.getString("encoding"))) {
                final var dataString = Base64.getDecoder()
                        .decode(file.getString("content").getBytes(StandardCharsets.UTF_8));
                try {
                    return OsmChangeReader.parseDataSetAndNotes(new ByteArrayInputStream(dataString),
                            NullProgressMonitor.INSTANCE);
                } catch (IllegalDataException e) {
                    ExceptionUtil.explainException(e);
                }

            }
        }
        return null;
    }

    /**
     * Parse review fields
     *
     * @param obj The object with the review fields
     * @return The parsed object
     */
    @Nonnull
    private static TaskReviewFields parseTaskReviewFields(JsonObject obj) {
        return new TaskReviewFields(optionalInteger(obj, "reviewStatus"), optionalLong(obj, "reviewRequestedBy"),
                optionalLong(obj, "reviewedBy"), optionalInstant(obj, "reviewedAt"),
                optionalLong(obj, "metaReviewedBy"), optionalInteger(obj, "metaReviewedStatus"),
                optionalInstant(obj, "metaReviewedAt"), optionalInstant(obj, "reviewStartedAt"),
                optionalLong(obj, "reviewClaimedBy"), optionalInstant(obj, "reviewClaimedAt"),
                obj.containsKey("additionalReviewers")
                        ? obj.getJsonArray("additionalReviewers").getValuesAs(JsonNumber.class).stream()
                                .mapToLong(JsonNumber::longValue).toArray()
                        : EMPTY_LONG);
    }

    /**
     * Parse mapillary images
     *
     * @param array The images to parse
     * @return The parsed images
     */
    @Nonnull
    private static MapillaryImages parseMapillaryImages(JsonArray array) {
        return new MapillaryImages(array.getValuesAs(JsonObject.class).stream()
                .map(obj -> new MapillaryImages.Image(obj.getString("key"), obj.getJsonNumber("lat").doubleValue(),
                        obj.getJsonNumber("lon").doubleValue(), obj.getString("url_320"), obj.getString("url_640"),
                        obj.getString("url_1024"), obj.getString("url_2048")))
                .toArray(MapillaryImages.Image[]::new));
    }

    /**
     * Parse an id
     *
     * @param id The id to parse
     * @return The parsed primitive id
     */
    @Nonnull
    private static PrimitiveId parseId(@Nonnull String id) {
        String[] parts = id.split("/");
        final var osmId = Long.parseLong(parts[1]);
        return switch (parts[0]) {
        case "node" -> new SimplePrimitiveId(osmId, OsmPrimitiveType.NODE);
        case "way" -> new SimplePrimitiveId(osmId, OsmPrimitiveType.WAY);
        case "relation" -> new SimplePrimitiveId(osmId, OsmPrimitiveType.RELATION);
        default -> throw new IllegalArgumentException(id);
        };
    }
}
