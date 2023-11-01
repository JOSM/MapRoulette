package org.openstreetmap.josm.plugins.maproulette.io.upload;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.data.APIDataSet;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Tagged;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.maproulette.api.enums.Difficulty;
import org.openstreetmap.josm.plugins.maproulette.api.enums.TaskStatus;
import org.openstreetmap.josm.plugins.maproulette.api.model.ClusteredPoint;
import org.openstreetmap.josm.plugins.maproulette.api.model.Point;
import org.openstreetmap.josm.plugins.maproulette.api.model.PointReview;
import org.openstreetmap.josm.plugins.maproulette.api.model.PublicUser;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.plugins.maproulette.gui.ModifiedObjects;
import org.openstreetmap.josm.plugins.maproulette.gui.ModifiedTask;
import org.openstreetmap.josm.plugins.maproulette.gui.layer.MapRouletteClusteredPointLayer;
import org.openstreetmap.josm.plugins.maproulette.util.MapRouletteConfig;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.testutils.annotations.BasicPreferences;
import org.openstreetmap.josm.testutils.annotations.Main;
import org.openstreetmap.josm.testutils.annotations.Projection;

/**
 * Test class for {@link EarlyUploadHook}
 */
@BasicPreferences
@Main
@Projection
@MapRouletteConfig
class EarlyUploadHookTest {
    private EarlyUploadHook hook;

    @BeforeEach
    void setUp() {
        this.hook = new EarlyUploadHook();
    }

    @ParameterizedTest
    @ValueSource(strings = {"9494185766_node_4", "9494185766", "node/9494185766", "node 9494185766@4"})
    void testRegexParsing(String title) {
        final var ds = new DataSet();
        final var node = TestUtils.newNode("");
        node.setCoor(LatLon.ZERO);
        node.setOsmId(9494185766L, 1);
        node.setModified(true);
        ds.addPrimitive(node);
        final var apiDataSet = new APIDataSet(ds);
        MainApplication.getLayerManager().addLayer(new OsmDataLayer(ds, "testRegexParsing", null));
        final var mrLayer = new MapRouletteClusteredPointLayer(new Bounds(0, 0, 0, 0), Collections.singleton(
                new ClusteredPoint(132279499L, 0L, "null", title, 0L, "parentName", new Point(0, 0),
                        "bounding", "blurb", Instant.EPOCH, Difficulty.NORMAL, 2, TaskStatus.CREATED, null,
                        Instant.EPOCH, 0L, new PublicUser(0L, null, "someone", Instant.EPOCH, null),
                        new PointReview(null, null, null, null, null, null, null, null, null), 0, null, false)
        ));
        MainApplication.getLayerManager().addLayer(mrLayer);
        Config.getPref().putInt("message." + EarlyUploadHook.PREF_CHECK_IF_FINISHED + ".value", 0);
        Config.getPref().putBoolean("message." + EarlyUploadHook.PREF_CHECK_IF_FINISHED, false);
        assertTrue(hook.checkUpload(apiDataSet));
        final var map = new HashMap<String, String>();
        hook.modifyChangesetTags(map);
        assertEquals("132279499", map.get("maproulette:tasks"));
    }

    /**
     * This checks to make certain that a lot of tasks do not go over the 255 character limit
     */
    @Test
    void testManyClosedTasks() {
        for (int i = 100_000_000; i < 100_000_050; i++) {
            ModifiedObjects.addModifiedTask(new ModifiedTask(new Task(i, "", null, null, 15318, null, null, null, null, null, null, null, null, null, 0, null, null, null, false, null, null), TaskStatus.FIXED, null, null, null, null));
        }
        final var map = new HashMap<String, String>();
        hook.modifyChangesetTags(map);
        assertEquals(4, map.size());
        assertAll("All values must be less than the max tag length",
                map.values().stream().map(s -> () -> assertTrue(s.length() < Tagged.MAX_TAG_LENGTH)));
        final var tasksString = map.get("maproulette:tasks") + ";" + map.get("maproulette:tasks:2");
        assertFalse(tasksString.startsWith(";"));
        assertFalse(tasksString.endsWith(";"));
        final var tasks = Stream.of(tasksString.split(";", -1)).mapToLong(Long::parseLong).toArray();
        final var sortedTasks = tasks.clone();
        Arrays.sort(sortedTasks);
        assertArrayEquals(sortedTasks, tasks);
        assertEquals(50, tasks.length);
    }

    /**
     * This checks to make certain we don't add maproulette tags when the user hasn't done anything yet
     */
    @Test
    void testNoChanges() {
        final var emptyMap = new HashMap<String, String>();
        hook.modifyChangesetTags(emptyMap);
        assertTrue(emptyMap.isEmpty());
    }
}