// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.plugins.maproulette.api.TaskAPI;
import org.openstreetmap.josm.plugins.maproulette.util.MapRouletteConfig;

@MapRouletteConfig
class MRGuiHelperTest {

    @Test
    void testGetInstructionTextMustache() {
        final var task = assertDoesNotThrow(() -> TaskAPI.get(147197178));
        final var instructions = MRGuiHelper.getInstructionText(task);
        assertEquals(
                """
                        <html><ol>
                        <li>Node 6318952236 has been tagged as a building indicating that it represents a building, but it is better to represent buildings with polygons or multipolygons. Please see if there is enough satellite imagery information to replace this node with a new polygon. If there is enough detail to draw this building as a polygon then add the polygon that represents the building and remove the building tag from this node or transfer the tags from this node to the new polygon, and then delete this node. See https://wiki.openstreetmap.org/wiki/Mapping_addresses_as_separate_nodes_or_by_adding_to_building_polygons.</li>
                        </ol>
                        </html>""",
                instructions);
    }
}