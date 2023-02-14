package org.openstreetmap.josm.plugins.maproulette.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutionException;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JTable;

import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapViewState;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.maproulette.actions.IgnoreAction;
import org.openstreetmap.josm.plugins.maproulette.util.LoggingHandler;
import org.openstreetmap.josm.plugins.maproulette.util.Main;
import org.openstreetmap.josm.plugins.maproulette.util.MapRouletteConfig;
import org.openstreetmap.josm.testutils.annotations.Projection;

/**
 * Test class for {@link TaskListPanel}
 */
@LoggingHandler
@Main
@MapRouletteConfig
@Projection
public class TaskListPanelTest {

    /**
     * Fire the ignore action
     *
     * @param panel      The panel to use
     * @param ignoreType The ignore action to use
     */
    public static void fireIgnoreAction(TaskListPanel panel, IgnoreAction.IgnoreType ignoreType) {
        final var table = ((JTable) ((JComponent) ((JComponent) ((JComponent) panel.getComponent(1 /* The scroll pane */))
                .getComponent(0 /* The viewport */)).getComponent(0 /* A panel */))
                .getComponent(2 /* The table */));
        final var menu = table.getComponentPopupMenu();
        final var menuItem = (JMenuItem) switch (ignoreType) {
            case IGNORE_TASK -> menu.getComponent(1);
            case IGNORE_CHALLENGE -> menu.getComponent(2);
        };
        menu.setInvoker(table);
        final var action = menuItem.getAction();
        assertEquals(ignoreType.getButtonText(), action.getValue(Action.NAME));
        action.actionPerformed(new ActionEvent(menuItem, 0, "ignore"));
    }

    @Test
    void testDuplicateKeyIssue() throws ExecutionException, InterruptedException {
        final var panel = new TaskListPanel();
        final var action = getDownloadAction(panel);
        final var bounds = new Bounds(36.0770518, -119.1139411, 36.0839216, -119.0992458);
        MainApplication.getLayerManager().addLayer(new OsmDataLayer(new DataSet(), "testDuplicateKeyIssue", null));
        new MockUp<MapViewState.MapViewRectangle>() {
            @Mock
            public Bounds getLatLonBoundsBox() {
                return bounds;
            }
        };
        action.actionPerformed(null);
        MainApplication.worker.submit(() -> { /* Sync thread */ }).get();
        action.actionPerformed(null);
        MainApplication.worker.submit(() -> { /* Sync thread */ }).get();
    }

    /**
     * Get the download action for a panel
     *
     * @param taskListPanel The panel to use
     * @return The download action
     */
    public static Action getDownloadAction(TaskListPanel taskListPanel) {
        final var action = ((JButton) ((JComponent) ((JComponent) taskListPanel.getComponent(2 /* The side buttons */))
                .getComponent(0 /* A singular JPanel */))
                .getComponent(0 /* The download button */)).getAction();
        assertEquals("org.openstreetmap.josm.plugins.maproulette.gui.TaskListPanel$DownloadDataAction", action.getClass().getName());
        return action;
    }
}