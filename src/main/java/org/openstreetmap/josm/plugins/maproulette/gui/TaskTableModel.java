// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.LayerManager;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.plugins.maproulette.api.model.ClusteredPoint;
import org.openstreetmap.josm.plugins.maproulette.api.model.Identifier;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.plugins.maproulette.api.model.TaskClusteredPoint;
import org.openstreetmap.josm.plugins.maproulette.gui.layer.MapRouletteClusteredPointLayer;

/**
 * A model for {@link Task} objects. This class implements many of the same semantics as {@link List}.
 */
final class TaskTableModel extends AbstractTableModel
        implements LayerManager.LayerChangeListener, Consumer<Map<Long, TaskClusteredPoint>> {
    @Serial
    private static final long serialVersionUID = 3498540089797609812L;
    /**
     * The list of downloaded tasks
     */
    private final List<TaskClusteredPoint> taskList = new ArrayList<>();

    @Override
    public int getRowCount() {
        return this.taskList.size();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return tr("MapRoulette Task");
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return ClusteredPoint.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return this.get(rowIndex);
    }

    /**
     * Get a specified point from the list
     *
     * @param index The list index
     * @return The point at that index
     */
    public TaskClusteredPoint get(int index) {
        return this.taskList.get(index);
    }

    @Override
    public void layerAdded(LayerManager.LayerAddEvent e) {
        if (e.getAddedLayer()instanceof MapRouletteClusteredPointLayer layer) {
            layer.addListener(this);
            this.accept(null);
        }
    }

    @Override
    public void layerRemoving(LayerManager.LayerRemoveEvent e) {
        if (e.getRemovedLayer()instanceof MapRouletteClusteredPointLayer layer) {
            layer.removeListener(this);
            this.accept(null);
        }
    }

    @Override
    public void layerOrderChanged(LayerManager.LayerOrderChangeEvent e) {
        // Don't care
    }

    @Override
    public void accept(Map<Long, TaskClusteredPoint> longClusteredPointMap) {
        if (SwingUtilities.isEventDispatchThread()) {
            this.taskList.clear();
            this.taskList.addAll(MainApplication.getLayerManager().getLayersOfType(MapRouletteClusteredPointLayer.class)
                    .stream().map(MapRouletteClusteredPointLayer::getTasks).flatMap(Collection::stream)
                    .sorted(Comparator.comparingLong(Identifier::id)).toList());
            this.fireTableDataChanged();
        } else {
            GuiHelper.runInEDT(() -> accept(longClusteredPointMap));
        }
    }

    /**
     * Stream the data
     *
     * @return The data as a stream
     */
    public Stream<TaskClusteredPoint> stream() {
        return this.taskList.stream();
    }

    /**
     * Get the index of an object
     *
     * @param clusteredPoint The object to get the index of
     * @return The list index of the point
     */
    public int indexOf(TaskClusteredPoint clusteredPoint) {
        return this.taskList.indexOf(clusteredPoint);
    }
}
