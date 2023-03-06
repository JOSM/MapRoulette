// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.data;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.plugins.maproulette.api.model.ChallengeExtra;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.plugins.maproulette.api_caching.ChallengeCache;
import org.openstreetmap.josm.plugins.maproulette.util.ExceptionDialogUtil;
import org.openstreetmap.josm.tools.Utils;

/**
 * A class for getting the primitives for a task
 */
public final class TaskPrimitives {
    /**
     * The pattern to use to check if the string only contains numbers
     */
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^\\d+$");

    /**
     * Hide the constructor
     */
    private TaskPrimitives() {
        // Hide constructor
    }

    /**
     * Get all the primitive ids for a task
     *
     * @param task The task to look through
     * @return The primitive ids from the task, if any
     */
    @Nonnull
    public static Collection<PrimitiveId> getPrimitiveIds(@Nullable Task task) {
        return getPrimitiveIdMap(task).keySet();
    }

    /**
     * Get the primitive id map for a task
     *
     * @param task The task to look for primitive mappings
     * @return The map of primitive id to primitive
     */
    @Nonnull
    public static Map<PrimitiveId, IPrimitive> getPrimitiveIdMap(@Nullable Task task) {
        if (task != null) {
            try {
                final var challenge = ChallengeCache.challenge(task.parentId());
                final var property = challenge.extra().osmIdProperty();
                if (!Utils.isBlank(property)) {
                    return getPrimitiveIdMap(task, property);
                }
            } catch (IOException ioException) {
                ExceptionDialogUtil.explainException(ioException);
            }
            for (var defaultProperty : ChallengeExtra.DEFAULT_OSM_ID_PROPERTIES) {
                final var primitives = getPrimitiveIdMap(task, defaultProperty);
                if (!primitives.isEmpty()) {
                    return primitives;
                }
            }
        }
        return Collections.emptyMap();
    }

    /**
     * Get the primitive ids for a task given a property
     *
     * @param task     The task to get ids from
     * @param property The property to use
     * @return The primitive ids
     */
    @Nonnull
    private static Map<PrimitiveId, IPrimitive> getPrimitiveIdMap(@Nullable Task task, @Nullable String property) {
        if (task != null && property != null) {
            final var map = new HashMap<PrimitiveId, IPrimitive>();
            for (var primitive : task.geometries().allPrimitives()) {
                if (primitive.hasTag(property)) {
                    final var primitiveId = getPrimitiveId(primitive, primitive.get(property));
                    if (primitiveId != null) {
                        map.put(primitiveId, primitive);
                    }
                }
            }
            return Collections.unmodifiableMap(map);
        }
        return Collections.emptyMap();
    }

    /**
     * Get the primitive id from a formatted string
     *
     * @param id The id to parse
     * @return The parsed primitive id
     */
    @Nullable
    private static PrimitiveId getPrimitiveId(@Nullable IPrimitive primitive, @Nullable String id) {
        if (id != null && primitive != null) {
            if (SimplePrimitiveId.ID_PATTERN.matcher(id).matches()) {
                SimplePrimitiveId.fromString(id);
            } else if (INTEGER_PATTERN.matcher(id).matches()) {
                final var type = Optional.ofNullable(primitive.get("type"))
                        .or(() -> Optional.ofNullable(primitive.get("@type")))
                        .or(() -> Optional.ofNullable(primitive.get("@osm_type"))).orElse("");
                final OsmPrimitiveType osmType = switch (type) {
                case "n", "node" -> OsmPrimitiveType.NODE;
                case "w", "way" -> OsmPrimitiveType.WAY;
                case "r", "relation" -> OsmPrimitiveType.RELATION;
                default -> primitive.getType(); // Fall back to the primitive type sent in
                };
                final var osmId = Long.parseLong(id);
                return new SimplePrimitiveId(osmId, osmType);
            }
        }
        return null;
    }
}
