// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openstreetmap.josm.plugins.maproulette.util.RecordAssertion.assertRecordsEqual;

import java.io.IOException;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.plugins.maproulette.api.TaskAPI;
import org.openstreetmap.josm.plugins.maproulette.api.enums.TaskStatus;
import org.openstreetmap.josm.plugins.maproulette.api.model.ClusteredPoint;
import org.openstreetmap.josm.plugins.maproulette.api.model.Point;
import org.openstreetmap.josm.plugins.maproulette.api.model.PointReview;
import org.openstreetmap.josm.plugins.maproulette.api.model.PublicUser;
import org.openstreetmap.josm.plugins.maproulette.util.MapRouletteConfig;

/**
 * Test class for {@link ClusteredPointParser}
 */
@MapRouletteConfig
class ClusteredPointParserTest {
    @Test
    void testTask136226437() throws IOException {
        final var box = TaskAPI.box(-108.4962538, 39.082404, -108.4962538, 39.082404,
                1_000, 0, true, null, null, false, true, true);
        assertEquals(1, box.length);
        final var photoMC = new PublicUser(11197, null, "Photo_MC", null, null);
        final var actual = box[0];
        final var expected = new ClusteredPoint(136226437L, -1L, "", "861207014_way_1",
                27887L, "United States | Polygon has self intersection", new Point(39.082404, -108.4962538),
                "", "", Instant.parse("2023-01-31T19:28:24.909Z"), null, 2, TaskStatus.TOO_HARD, null,
                Instant.parse("2022-10-18T03:19:56.028Z"), 923162L, photoMC,
                new PointReview(1, photoMC, new PublicUser(9724, null, "Jenn_Bh", null, null),
                        Instant.parse("2022-10-18T07:09:14.455Z"), null, null,
                        null, Instant.parse("2022-10-18T07:08:52.469Z"), new long[0]), 0, null, false);
        assertRecordsEqual(expected, actual);
    }
}
