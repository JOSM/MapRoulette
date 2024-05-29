// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.plugins.maproulette.api.TaskAPI;
import org.openstreetmap.josm.plugins.maproulette.util.MapRouletteConfig;

/**
 * Test class for {@link TaskPrimitives}
 */
@MapRouletteConfig
class TaskPrimitivesTest {
    @Test
    void testGetPrimitiveIdsNode() {
        final var task = assertDoesNotThrow(() -> TaskAPI.start(132279499));
        assertFalse(TaskPrimitives.getPrimitiveIds(task).isEmpty());
        assertEquals(1, TaskPrimitives.getPrimitiveIds(task).size());
        final var nodeId = TaskPrimitives.getPrimitiveIds(task).iterator().next();
        final var expectedId = new SimplePrimitiveId(9494185766L, OsmPrimitiveType.NODE);
        assertEquals(expectedId, nodeId);
    }

    @Test
    void testGetPrimitiveIdsNull() {
        assertTrue(TaskPrimitives.getPrimitiveIds(null).isEmpty());
    }

    @Test
    void testGetPrimitiveIdsNoIdentifier() {
        final var task = assertDoesNotThrow(() -> TaskAPI.start(132279499));
        task.geometries().allPrimitives().forEach(p -> p.remove("osmIdentifier"));
        assertTrue(TaskPrimitives.getPrimitiveIds(task).isEmpty());
    }
}