// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui.task.list;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JTable;

import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.maproulette.api.TaskAPI;
import org.openstreetmap.josm.plugins.maproulette.api.enums.TaskStatus;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.plugins.maproulette.gui.ModifiedObjects;
import org.openstreetmap.josm.plugins.maproulette.gui.layer.MapRouletteClusteredPointLayer;
import org.openstreetmap.josm.plugins.maproulette.util.ExceptionDialogUtil;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Lock tasks
 */
final class LockUnlockTaskAction extends JosmAction {
    /**
     * The serial UID for this component
     */
    @Serial
    private static final long serialVersionUID = 3686177310606460566L;
    /**
     * The table to update
     */
    private final JTable table;

    /**
     * Create a new action
     *
     * @param table The table to update
     */
    LockUnlockTaskAction(JTable table) {
        super(tr("Start Task"), "lock", tr("Start MapRoulette Tasks"),
                Shortcut.registerShortcut("maproulette:start_stop_tasks", tr("MapRoulette: Start or Stop Tasks"),
                        KeyEvent.CHAR_UNDEFINED, Shortcut.NONE),
                false);
        Objects.requireNonNull(table, "table");
        this.table = table;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Boolean.TRUE.equals(getValue("lock"))) {
            lockTasks();
        } else {
            unlockTasks();
        }
    }

    /**
     * Perform lock actions
     */
    private void lockTasks() {
        final var selected = this.table.getSelectionModel().getSelectedIndices();
        final var model = (TaskTableModel) this.table.getModel();
        final var selectedTasks = new ArrayList<Task>();
        for (int index : selected) {
            try {
                final var i = table.getRowSorter().convertRowIndexToModel(index);
                final var task = model.get(i);
                final var lockedTask = TaskAPI.start(task.id());
                ModifiedObjects.addLockedTask(lockedTask);
                selectedTasks.add(lockedTask);
            } catch (IOException e) {
                ExceptionDialogUtil.explainException(e);
            }
        }

        ((TaskTableModel) this.table.getModel()).fireTableDataChanged();
        reselect(selected);
        if (!selectedTasks.isEmpty()) {
            final List<IPrimitive> primitiveList = new ArrayList<>();
            for (var task : selectedTasks) {
                if (task.geometries().allPrimitives().isEmpty()) {
                    primitiveList.add(task.location());
                } else {
                    primitiveList.addAll(task.geometries().allPrimitives());
                }
            }
            AutoScaleAction.zoomTo(primitiveList);
        }
    }

    /**
     * Perform unlock actions
     */
    private void unlockTasks() {
        final var selected = this.table.getSelectedRows();
        final var data = (TaskTableModel) this.table.getModel();
        for (var index : selected) {
            final var i = this.table.getRowSorter().convertRowIndexToModel(index);
            final var cluster = data.get(i);
            final var task = ModifiedObjects.getLockedTask(cluster.id());
            try {
                final var unlockedTask = TaskAPI.release(cluster.id());
                if (task != null && task.id() == unlockedTask.id()) {
                    final var modified = ModifiedObjects.getModifiedTask(task.id());
                    if (modified != null && modified.status() != TaskStatus.FIXED) {
                        TaskAPI.updateStatus(task.id(), modified.status(), modified.comment(), modified.tags(),
                                modified.reviewRequested(), modified.completionResponses());
                        ModifiedObjects.removeModifiedTask(modified);
                    }
                    ModifiedObjects.removeLockedTask(task);
                }
                final var newPoint = TaskAPI.get(cluster.id());
                MainApplication.getLayerManager().getLayersOfType(MapRouletteClusteredPointLayer.class)
                        .forEach(layer -> layer.refreshTasks(Map.of(newPoint.id(), newPoint)));
            } catch (IOException ioException) {
                ExceptionDialogUtil.explainException(ioException);
            }
        }
        ((TaskTableModel) this.table.getModel()).fireTableDataChanged();
        reselect(selected);
    }

    /**
     * Reselect a selection after indicating that all data in the table changed
     *
     * @param selected The selection to reselect
     */
    private void reselect(int[] selected) {
        final var selectionModel = this.table.getSelectionModel();
        selectionModel.clearSelection();
        selectionModel.setValueIsAdjusting(true);
        try {
            for (var index : selected) {
                selectionModel.setSelectionInterval(index, index);
            }
        } finally {
            selectionModel.setValueIsAdjusting(false);
        }
    }

    @Override
    protected void updateEnabledState() {
        if (this.table != null && !this.table.getSelectionModel().isSelectionEmpty()) {
            super.setEnabled(true);
            for (var i : this.table.getSelectedRows()) {
                final var index = this.table.getRowSorter().convertRowIndexToModel(i);
                final var point = ((TaskTableModel) this.table.getModel()).get(index);
                if (ModifiedObjects.getLockedTask(point.id()) == null) {
                    this.putValue(NAME, tr("Start Task"));
                    this.setTooltip(tr("Start MapRoulette Tasks"));
                    new ImageProvider("lock").getResource().attachImageIcon(this);
                    putValue("lock", true);
                    return;
                }
            }
            putValue("lock", false);
            this.putValue(NAME, tr("Stop Task"));
            this.setTooltip(tr("Stop MapRoulette Tasks"));
            new ImageProvider("save").getResource().attachImageIcon(this);
        } else {
            super.setEnabled(false);
        }
    }
}
