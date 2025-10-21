// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.parsers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openstreetmap.josm.plugins.maproulette.util.RecordAssertion.assertRecordsEqual;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.coor.ILatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.plugins.maproulette.api.TaskAPI;
import org.openstreetmap.josm.plugins.maproulette.api.enums.TaskStatus;
import org.openstreetmap.josm.plugins.maproulette.api.model.ElementCreate;
import org.openstreetmap.josm.plugins.maproulette.api.model.ElementTagChange;
import org.openstreetmap.josm.plugins.maproulette.api.model.ElementUpdate;
import org.openstreetmap.josm.plugins.maproulette.api.model.OSMChange;
import org.openstreetmap.josm.plugins.maproulette.api.model.Point;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.plugins.maproulette.api.model.TaskReviewFields;
import org.openstreetmap.josm.plugins.maproulette.util.MapRouletteConfig;

/**
 * Test class for {@link TaskParser}
 */
@MapRouletteConfig
class TaskParserTest {
    /**
     * Check that a task is correctly parsed
     */
    @Test
    void testTaskParsing135045992() {
        final var task = assertDoesNotThrow(() -> TaskAPI.start(135045992));
        final var expected = new Task(135045992, "way/131190351", Instant.parse("2022-08-06T15:23:36.531Z"),
                Instant.parse("2022-08-06T15:23:36.531Z"), 24092, "", new Point(36.0819446365856, -119.104608317353),
                task.geometries(),
                new OSMChange(new ElementCreate[0],
                        new ElementUpdate[] {
                                new ElementUpdate(131190351L, OsmPrimitiveType.WAY, Integer.MIN_VALUE,
                                        new ElementTagChange(Map.of("landuse", "farmland", "FMMP_modified", "yes"),
                                                new String[0])),
                                new ElementUpdate(131190351L, OsmPrimitiveType.WAY, Integer.MIN_VALUE,
                                        new ElementTagChange(Collections.emptyMap(), new String[] { "source" })) }),
                TaskStatus.CREATED, null, null, null,
                new TaskReviewFields(null, null, null, null, null, null, null, null, null, null), 0, -1L, null, null,
                false, null, "");
        assertRecordsEqual(expected, task);
        // Check geometries
        final var way = task.geometries().getWays().iterator().next();
        assertAll("Tags were improperly parsed", () -> assertEquals("way/131190351", way.get("@id")),
                () -> assertEquals("28.9408234929", way.get("acres")),
                () -> assertEquals("http://www.consrv.ca.gov/dlrp/fmmp/products/Pages/DownloadGISdata.aspx",
                        way.get("source")),
                () -> assertEquals("farmland", way.get("landuse")),
                () -> assertEquals("Tulare", way.get("addr:county")),
                () -> assertEquals("Farmland Mapping and Monitoring Program", way.get("attribution")),
                () -> assertEquals("farmland of local importance", way.get("description")),
                () -> assertEquals("yes", way.get("FMMP_modified")),
                () -> assertEquals("no", way.get("FMMP_reviewed")));
    }

    /**
     * Check that the geometry for a task is correctly parsed
     */
    @Test
    void testTaskParsing134808786() {
        final var task = assertDoesNotThrow(() -> TaskAPI.start(134808786));
        final var expected = new Task(134808786, "node/-103005", Instant.parse("2022-08-04T15:44:15.335Z"),
                Instant.parse("2022-08-04T15:44:15.335Z"), 28467, "", new Point(43.3719671, -82.9782727),
                task.geometries(), task.cooperativeWork(), TaskStatus.CREATED, null, null, null,
                new TaskReviewFields(null, null, null, null, null, null, null, null, null, null), 0, -1L, null, null,
                false, null, "");
        assertRecordsEqual(expected, task);
        final var actualData = expected.cooperativeWorkAsOsc();
        assertNotNull(actualData);
        assertTrue(actualData.b.getNotes().isEmpty());
        final var ds = actualData.a;
        assertEquals(1, ds.allPrimitives().size());
        assertEquals(1, ds.getNodes().size());
        final var node = ds.getNodes().iterator().next();
        assertEquals(43.3719671, node.lat(), ILatLon.MAX_SERVER_PRECISION);
        assertEquals(-82.9782727, node.lon(), ILatLon.MAX_SERVER_PRECISION);
        assertEquals(Map.of("gnis:feature_id", "624965", "name", "Duff Creek", "waterway", "stream"), node.getKeys());
        assertTrue(node.isNew());
    }

    @Test
    void testTaskParsing136226437() {
        final var task = assertDoesNotThrow(() -> TaskAPI.start(136226437));
        final var expected = new Task(136226437L, "861207014_way_1", Instant.parse("2022-09-15T18:50:39.354Z"),
                Instant.parse("2022-10-18T03:19:56.028Z"), 27887, "", new Point(39.082404, -108.4962538),
                task.geometries(), task.cooperativeWork(), TaskStatus.TOO_HARD,
                Instant.parse("2022-10-18T03:19:56.028Z"), 923162L, 11197L,
                new TaskReviewFields(1, 11197L, 9724L, Instant.parse("2022-10-18T07:09:14.455Z"), null, null, null,
                        Instant.parse("2022-10-18T07:08:52.469Z"), null, null),
                0, -1L, null, null, false, null, "");
        assertRecordsEqual(expected, task);
    }
}
