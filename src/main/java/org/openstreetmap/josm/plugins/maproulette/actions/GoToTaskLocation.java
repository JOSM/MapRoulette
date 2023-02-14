// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.util.ArrayDeque;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.maproulette.api.model.ClusteredPoint;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Go to a task location
 */
public class GoToTaskLocation extends JosmAction {
    /**
     * The serial UID for this component
     */
    @Serial
    private static final long serialVersionUID = -3081266863507316569L;

    /**
     * Create a new action
     */
    public GoToTaskLocation() {
        super(tr("Zoom to selection"), "dialogs/autoscale/selection", tr("Zoom to selection"),
                Shortcut.registerShortcut("maproulette:zoom_to_task_selection",
                        tr("MapRoulette: Zoom to task selection"), KeyEvent.CHAR_UNDEFINED, Shortcut.NONE),
                false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final var locations = new ArrayDeque<BBox>();
        final var component = ((JPopupMenu) ((JMenuItem) e.getSource()).getParent()).getInvoker();
        final List<?> objects = ActionUtils.getSelectedItems(component);
        for (var obj : objects) {
            if (obj instanceof Task t && t.location() != null) {
                for (IPrimitive p : t.geometries().allNonDeletedPrimitives()) {
                    locations.add(p.getBBox());
                }
            } else if (obj instanceof ClusteredPoint p) {
                locations.add(p.location().getBBox());
            }
        }
        if (!locations.isEmpty()) {
            final var bbox = locations.pop();
            final var bounds = new Bounds(bbox.getBottomRight());
            bounds.extend(bbox.getTopLeft());
            while (!locations.isEmpty()) {
                final var tBBox = locations.pop();
                bounds.extend(tBBox.getTopLeft());
                bounds.extend(tBBox.getBottomRight());
            }
            final var mv = MainApplication.getMap().mapView;
            if (bounds.isCollapsed()) {
                mv.zoomTo(bounds.getMax());
                while (mv.getDist100Pixel() > 30) {
                    mv.zoomIn();
                }
            } else {
                mv.zoomTo(bounds);
            }
        }
    }
}
