// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.parsers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.plugins.maproulette.api.TaskAPI;
import org.openstreetmap.josm.plugins.maproulette.util.MapRouletteConfig;

/**
 * Test class for {@link GeometryParser}
 */
@MapRouletteConfig
class GeometryParserTest {
    @Test
    void testGeometryTask32402008() {
        final var task = assertDoesNotThrow(() -> TaskAPI.start(32402008));
        assertNotNull(task);
        assertAll(() -> assertNotNull(task.geometries()),
                () -> assertEquals(1, task.geometries().allPrimitives().size()),
                () -> assertEquals(1, task.geometries().getNodes().size()));
        final var node = task.geometries().getNodes().iterator().next();
        assertNotNull(node);
        assertAll(() -> assertEquals("crossing", node.get("highway")));
    }
}