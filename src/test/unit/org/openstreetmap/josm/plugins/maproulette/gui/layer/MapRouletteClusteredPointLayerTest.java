// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui.layer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.MouseEvent;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.JComponent;

import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapViewState;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.gui.widgets.FilterField;
import org.openstreetmap.josm.plugins.maproulette.actions.IgnoreAction;
import org.openstreetmap.josm.plugins.maproulette.api.model.Identifier;
import org.openstreetmap.josm.plugins.maproulette.gui.TaskListPanelTest;
import org.openstreetmap.josm.plugins.maproulette.gui.task.list.TaskListPanel;
import org.openstreetmap.josm.plugins.maproulette.util.MapRouletteConfig;
import org.openstreetmap.josm.testutils.annotations.BasicPreferences;
import org.openstreetmap.josm.testutils.annotations.HTTP;
import org.openstreetmap.josm.testutils.annotations.Main;
import org.openstreetmap.josm.testutils.annotations.Projection;

import mockit.Mock;
import mockit.MockUp;

/**
 * Test class for {@link MapRouletteClusteredPointLayer}
 */
@BasicPreferences
@HTTP
@Main
@MapRouletteConfig
@Projection
class MapRouletteClusteredPointLayerTest {
    /**
     * Make certain that the selection code works properly when a task is hidden
     */
    @Test
    void testShiftSelection() throws ExecutionException, InterruptedException, TimeoutException {
        MainApplication.getLayerManager().addLayer(new OsmDataLayer(new DataSet(), "testShiftSelection", null));
        final var bounds = new Bounds(36.0770518, -119.1139411, 36.0839216, -119.0992458);
        new MockUp<MapViewState.MapViewRectangle>() {
            @Mock
            public Bounds getLatLonBoundsBox() {
                return bounds;
            }
        };
        final var panel = new TaskListPanel();
        try {
            MainApplication.getMap().addToggleDialog(panel);
            TaskListPanelTest.getDownloadAction(panel).actionPerformed(null);
            sync();
            final var layers = MainApplication.getLayerManager().getLayersOfType(MapRouletteClusteredPointLayer.class);
            assertEquals(1, layers.size());
            final var layer = layers.get(0);
            assertTrue(panel.getSelected().isEmpty());
            // Check and make certain that "simple" click events work properly
            layer.mouseClicked(generateMouseEvent(false, 36.0778797, -119.1075725));
            assertEquals(1, panel.getSelected().size());
            assertEquals(133361784L, panel.getSelected().iterator().next().id());
            layer.mouseClicked(generateMouseEvent(false, 36.080299, -119.1074128));
            assertEquals(1, panel.getSelected().size());
            assertEquals(133361785L, panel.getSelected().iterator().next().id());
            // Now what happens with shift events?
            layer.mouseClicked(generateMouseEvent(true, 36.0778797, -119.1075725));
            assertEquals(2, panel.getSelected().size());
            assertTrue(panel.getSelected().stream().mapToLong(Identifier::id)
                    .allMatch(id -> id == 133361784L || id == 133361785L));
            assertEquals(2, panel.getSelected().stream().mapToLong(Identifier::id).distinct().count());
            // Now, let us hide the challenge for the objects we have selected
            TaskListPanelTest.fireIgnoreAction(panel, IgnoreAction.IgnoreType.IGNORE_CHALLENGE);
            assertEquals(0, panel.getSelected().size());
            layer.mouseClicked(generateMouseEvent(true, 36.0821182637293, -119.104558450006));
            assertEquals(0, panel.getSelected().size());
        } finally {
            MainApplication.getMap().removeToggleDialog(panel);
        }
    }

    /**
     * Non-regression test for <a href="https://josm.openstreetmap.de/ticket/24415">#24415</a>
     *
     * This occurs when the user clicks on a hidden task (from the filter)
     */
    @Test
    void testNonRegression24415() throws InterruptedException, ExecutionException, TimeoutException {
        MainApplication.getLayerManager().addLayer(new OsmDataLayer(new DataSet(), "testShiftSelection", null));
        final var bounds = new Bounds(36.0770518, -119.1139411, 36.0839216, -119.0992458);
        new MockUp<MapViewState.MapViewRectangle>() {
            @Mock
            public Bounds getLatLonBoundsBox() {
                return bounds;
            }
        };
        final var panel = new TaskListPanel();
        try {
            MainApplication.getMap().addToggleDialog(panel);
            TaskListPanelTest.getDownloadAction(panel).actionPerformed(null);
            sync();
            final var layers = MainApplication.getLayerManager().getLayersOfType(MapRouletteClusteredPointLayer.class);
            assertEquals(1, layers.size());
            final var layer = layers.get(0);
            assertTrue(panel.getSelected().isEmpty());
            // Check and make certain that "simple" click events work properly
            layer.mouseClicked(generateMouseEvent(false, 36.0778797, -119.1075725));
            sync();
            assertEquals(1, panel.getSelected().size());
            assertEquals(133361784L, panel.getSelected().iterator().next().id());
            layer.mouseClicked(generateMouseEvent(false, 36.080299, -119.1074128));
            assertEquals(1, panel.getSelected().size());
            assertEquals(133361785L, panel.getSelected().iterator().next().id());
            // Now what happens when we filter the images
            final FilterField filter = assertInstanceOf(FilterField.class,
                    ((JComponent) ((JComponent) ((JComponent) panel.getComponent(1)).getComponent(0)).getComponent(0))
                            .getComponent(0));
            filter.setText("foobar12345678912345678");
            sync();
            assertDoesNotThrow(() -> layer.mouseClicked(generateMouseEvent(false, 36.0778797, -119.1075725)));
            assertEquals(0, panel.getSelected().size());
        } finally {
            MainApplication.getMap().removeToggleDialog(panel);
        }
    }

    private static MouseEvent generateMouseEvent(boolean shift, double lat, double lon) {
        final var mv = MainApplication.getMap().mapView;
        while (mv.getScale() > 10) {
            mv.zoomIn();
        }
        final var ll = new LatLon(lat, lon);
        mv.zoomTo(ll);
        final var point = mv.getPoint(ll);
        return new MouseEvent(MainApplication.getMap(), UUID.randomUUID().hashCode(), System.currentTimeMillis(),
                shift ? MouseEvent.SHIFT_DOWN_MASK : 0, point.x, point.y, 1, false, MouseEvent.BUTTON1);
    }

    private static void sync() throws InterruptedException, ExecutionException, TimeoutException {
        MainApplication.worker.submit(() -> {
            /* Sync thread */ }).get(5, TimeUnit.SECONDS);
        final CountDownLatch cdl = new CountDownLatch(2);
        GuiHelper.runInEDTAndWait(cdl::countDown);
        MainApplication.worker.execute(cdl::countDown);
        assertTrue(cdl.await(1, TimeUnit.SECONDS));
    }
}