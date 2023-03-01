// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui.task.list;

import static org.openstreetmap.josm.plugins.maproulette.config.MapRouletteConfig.getBaseUrl;
import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.downloadtasks.DownloadParams;
import org.openstreetmap.josm.data.preferences.CachingProperty;
import org.openstreetmap.josm.data.preferences.NamedColorProperty;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.LayerManager;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.gui.widgets.FilterField;
import org.openstreetmap.josm.plugins.maproulette.actions.GoToTaskLocation;
import org.openstreetmap.josm.plugins.maproulette.actions.IgnoreAction;
import org.openstreetmap.josm.plugins.maproulette.actions.downloadtasks.MapRouletteDownloadTaskBox;
import org.openstreetmap.josm.plugins.maproulette.api.MRColors;
import org.openstreetmap.josm.plugins.maproulette.api.model.ClusteredPoint;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.plugins.maproulette.api.model.TaskClusteredPoint;
import org.openstreetmap.josm.plugins.maproulette.api_caching.ChallengeCache;
import org.openstreetmap.josm.plugins.maproulette.api_caching.TaskCache;
import org.openstreetmap.josm.plugins.maproulette.gui.ModifiedObjects;
import org.openstreetmap.josm.plugins.maproulette.gui.layer.MapRouletteClusteredPointLayer;
import org.openstreetmap.josm.plugins.maproulette.gui.preferences.MapRoulettePreferences;
import org.openstreetmap.josm.plugins.maproulette.gui.task.current.CurrentTaskPanel;
import org.openstreetmap.josm.plugins.maproulette.gui.widgets.DefaultPanelListCellRenderer;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.OpenBrowser;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * A panel showing the task list for the downloaded bbox
 * Note: This may be folded into a {@link javax.swing.JTabbedPane}.
 */
public final class TaskListPanel extends ToggleDialog
        implements LayerManager.LayerChangeListener, Consumer<Collection<TaskClusteredPoint>> {
    /**
     * The serial UID for this component
     */
    @Serial
    private static final long serialVersionUID = -8983504332024481559L;
    /**
     * The underlying table
     */
    private final JTable table;
    /**
     * The underlying model
     */
    private final TaskTableModel model;

    /**
     * Create a new task list panel
     */
    public TaskListPanel() {
        super(tr("MapRoulette Tasks"), "user_no_image.png", tr("Downloaded MapRoulette tasks"),
                Shortcut.registerShortcut("maproulette:task_window", tr("Downloaded MapRoulette tasks"),
                        KeyEvent.CHAR_UNDEFINED, Shortcut.NONE),
                200, false, MapRoulettePreferences.class);
        model = new TaskTableModel();
        MainApplication.getLayerManager().addAndFireLayerChangeListener(model);
        MainApplication.getLayerManager().addAndFireLayerChangeListener(this);
        table = new JTable(model);
        final var downloadButton = new SideButton(new DownloadDataAction());
        final var lockUnlockButton = new SideButton(new LockUnlockTaskAction(table));
        final var browseButton = new SideButton(new OpenInBrowserAction(table));
        final var tableRowSorter = new TableRowSorter<>(model);
        final var menu = new JPopupMenu();
        final var filterField = new FilterField();
        final var panel = new JPanel(new GridBagLayout());
        panel.add(filterField, GBC.eol().fill(GBC.HORIZONTAL));
        panel.add(table.getTableHeader(), GBC.eol().anchor(GBC.LINE_START).fill(GBC.HORIZONTAL));
        panel.add(table, GBC.eol().anchor(GBC.LINE_START).fill(GBC.BOTH));
        final RowFilter<TaskTableModel, Integer> defaultFilter = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends TaskTableModel, ? extends Integer> entry) {
                final var point = (TaskClusteredPoint) entry.getValue(0);
                return !TaskCache.isHidden(point);
            }
        };
        filterField.filter(expr -> {
            expr = expr.replace("+", "\\+");
            final ArrayList<RowFilter<? super TaskTableModel, ? super Integer>> andFilters = new ArrayList<>();
            andFilters.add(defaultFilter);
            // split search string on whitespace, do case-insensitive AND search
            for (var word : expr.split("\\s+", -1)) {
                andFilters.add(RowFilter.regexFilter("(?i)" + word));
            }
            tableRowSorter.setRowFilter(andFilters.size() > 1 ? RowFilter.andFilter(andFilters) : defaultFilter);
        });
        menu.add(new GoToTaskLocation());
        menu.add(new IgnoreAction(IgnoreAction.IgnoreType.IGNORE_TASK));
        menu.add(new IgnoreAction(IgnoreAction.IgnoreType.IGNORE_CHALLENGE));
        table.setComponentPopupMenu(menu);
        tableRowSorter.setRowFilter(defaultFilter);
        tableRowSorter.setComparator(0,
                Comparator.comparing(TaskListPanel::getParentName).thenComparingInt(TaskClusteredPoint::priority)
                        .thenComparing(TaskListPanel::getTitle).thenComparingLong(TaskClusteredPoint::id));
        table.setRowSorter(tableRowSorter);
        table.setDefaultRenderer(ClusteredPoint.class, new TaskListCellRenderer());
        table.getSelectionModel().addListSelectionListener(l -> {
            ((LockUnlockTaskAction) lockUnlockButton.getAction()).updateEnabledState();
            ((OpenInBrowserAction) browseButton.getAction()).updateEnabledState();
            final Task task;
            if (!l.getValueIsAdjusting() && this.table.getRowCount() > table.getSelectedRow()
                    && this.table.getSelectedRow() >= 0) {
                final var index = table.getRowSorter().convertRowIndexToModel(table.getSelectedRow());
                final var taskId = ((TaskClusteredPoint) table.getModel().getValueAt(index, -1)).id();
                task = ModifiedObjects.getLockedTask(taskId);
            } else {
                task = null;
            }
            if (!l.getValueIsAdjusting()) {
                if (task != null) {
                    if (MainApplication.getMap().getToggleDialog(CurrentTaskPanel.class) == null) {
                        final var currentTaskPanel = new CurrentTaskPanel();
                        MainApplication.getMap().addToggleDialog(currentTaskPanel);
                    }
                    MainApplication.getMap().getToggleDialog(CurrentTaskPanel.class).refreshModel(task);
                } else if (MainApplication.getMap().getToggleDialog(CurrentTaskPanel.class) != null) {
                    MainApplication.getMap().getToggleDialog(CurrentTaskPanel.class).refreshModel(null);
                }
            }
        });
        tableRowSorter.toggleSortOrder(0); // Start with an initial sort
        super.createLayout(panel, true, Arrays.asList(downloadButton, lockUnlockButton, browseButton));
    }

    /**
     * Get the parent name
     *
     * @param point The point to get the parent name for
     * @return The parent name
     */
    private static String getParentName(TaskClusteredPoint point) {
        if (point instanceof ClusteredPoint clusteredPoint) {
            return clusteredPoint.parentName();
        } else if (point instanceof Task task) {
            return ChallengeCache.challenge(task.parentId()).name();
        } else {
            throw new IllegalArgumentException("Unknown class type: " + point.getClass());
        }
    }

    /**
     * Get the title for a task
     *
     * @param point The point to get the title for
     * @return The title
     */
    private static String getTitle(TaskClusteredPoint point) {
        if (point instanceof ClusteredPoint clusteredPoint) {
            return clusteredPoint.title();
        } else if (point instanceof Task task) {
            return task.name();
        } else {
            throw new IllegalArgumentException("Unknown class type: " + point.getClass());
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        MainApplication.getLayerManager().removeAndFireLayerChangeListener(this.model);
    }

    /**
     * Get selected objects
     *
     * @return The selected objects
     */
    public Collection<TaskClusteredPoint> getSelected() {
        final var selected = this.table.getSelectedRows();
        final var set = new HashSet<TaskClusteredPoint>(selected.length);
        for (var i : selected) {
            set.add((TaskClusteredPoint) this.table.getValueAt(i, 0));
        }
        return set;
    }

    @Override
    public void accept(Collection<TaskClusteredPoint> selected) {
        final int[] toSelect = selected.stream().mapToInt(((TaskTableModel) table.getModel())::indexOf)
                .filter(i -> i >= 0).map(i -> table.getRowSorter().convertRowIndexToView(i)).sorted().distinct()
                .toArray();
        final var selModel = table.getSelectionModel();
        selModel.clearSelection();
        for (int i : toSelect) { // Not ideal. Probably won't be a perf issue though.
            selModel.addSelectionInterval(i, i);
        }
        if (toSelect.length > 0) {
            // Force an update of the row height
            table.getCellRenderer(toSelect[0], 0).getTableCellRendererComponent(table, table.getValueAt(toSelect[0], 0),
                    true, true, toSelect[0], 0);
            table.scrollRectToVisible(table.getCellRect(toSelect[0], 0, true));
        }
    }

    @Override
    public void layerAdded(LayerManager.LayerAddEvent e) {
        if (e.getAddedLayer()instanceof MapRouletteClusteredPointLayer layer) {
            layer.addSelectionListener(this);
        }
    }

    @Override
    public void layerRemoving(LayerManager.LayerRemoveEvent e) {
        if (e.getRemovedLayer()instanceof MapRouletteClusteredPointLayer layer) {
            layer.removeSelectionListener(this);
        }
    }

    @Override
    public void layerOrderChanged(LayerManager.LayerOrderChangeEvent e) {
        // Ignore
    }

    /**
     * Open task in browser
     */
    private static final class OpenInBrowserAction extends JosmAction {
        /**
         * The serial UID for this component
         */
        @Serial
        private static final long serialVersionUID = 6133894141191468929L;
        /**
         * The table to use
         */
        private final JTable table;

        /**
         * Open a task in the browser
         *
         * @param table The originating table
         */
        OpenInBrowserAction(JTable table) {
            super(tr("Open Task in browser"), "presets/misc/contact", tr("Open MapRoulette Tasks in browser"),
                    Shortcut.registerShortcut("maproulette:open_tasks_in_browser",
                            tr("MapRoulette: Open Tasks in browser"), KeyEvent.CHAR_UNDEFINED, Shortcut.NONE),
                    false);
            Objects.requireNonNull(table, "table");
            this.table = table;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final var model = (TaskTableModel) this.table.getModel();
            if (this.table.getSelectedRows().length < 10) {
                for (int index : this.table.getSelectedRows()) {
                    final var i = this.table.getRowSorter().convertRowIndexToModel(index);
                    final var task = model.get(i);
                    final var url = getBaseUrl().replace("api/v2", "") + "challenge/" + task.parentId() + "/task/"
                            + task.id();
                    OpenBrowser.displayUrl(url);
                }
            }
        }

        @Override
        protected void updateEnabledState() {
            this.setEnabled(this.table != null && this.table.getSelectedRowCount() < 10
                    && this.table.getSelectedRowCount() > 0);
        }
    }

    /**
     * Download data from MapRoulette
     */
    private static final class DownloadDataAction extends JosmAction {
        /**
         * The serial UID for this component
         */
        @Serial
        private static final long serialVersionUID = -4078764340309276574L;

        /**
         * Create a new action
         */
        DownloadDataAction() {
            super(tr("Download Data"), "download", tr("Download MapRoulette Tasks"),
                    Shortcut.registerShortcut("maproulette:download_tasks", tr("MapRoulette: Download Tasks"),
                            KeyEvent.CHAR_UNDEFINED, Shortcut.NONE),
                    false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.setEnabled(false); // Keep the user from hitting the download button multiple times in short order
            MainApplication.worker.execute(this::download);
        }

        /**
         * Perform the download in a background thread
         */
        private void download() {
            final var bounds = MainApplication.getMap().mapView.getState().getViewArea().getLatLonBoundsBox();
            new MapRouletteDownloadTaskBox().download(new DownloadParams().withNewLayer(false), bounds,
                    NullProgressMonitor.INSTANCE);
            MainApplication.worker.submit(() -> GuiHelper.runInEDT(() -> this.setEnabled(true)));
        }
    }

    /**
     * A renderer for the task list
     */
    private static class TaskListCellRenderer extends DefaultPanelListCellRenderer<TaskClusteredPoint> {
        /**
         * The serial UID for this component
         */
        @Serial
        private static final long serialVersionUID = -4182177619455408415L;
        private static final CachingProperty<Color> COLOR_MODIFIED = new NamedColorProperty(
                marktr("MapRoulette: Task Modified"), Color.GREEN.brighter()).cached();
        private static final CachingProperty<Color> COLOR_LOCKED = new NamedColorProperty(
                marktr("MapRoulette: Task Locked"), Color.CYAN.brighter()).cached();

        private final JLabel singleLine = new JLabel();
        private final JLabel priority = new JLabel();
        private final JLabel difficulty = new JLabel();
        private final JLabel modified = new JLabel();
        private final JLabel type = new JLabel();
        private final JLabel status = new JLabel();

        /**
         * Create a new renderer
         */
        TaskListCellRenderer() {
            super(TaskClusteredPoint.class);
            this.setLayout(new GridBagLayout());
            final var gbc = GBC.eol().anchor(GBC.LINE_START).fill(GBC.HORIZONTAL);
            this.add(this.singleLine, gbc);
            this.add(this.priority, gbc);
            this.add(this.difficulty, gbc);
            this.add(this.modified, gbc);
            this.add(this.type, gbc);
            this.add(this.status, gbc);
        }

        @Override
        protected void addRenderComponents(TaskClusteredPoint value, boolean isSelected, boolean cellHasFocus) {
            this.singleLine.setText(TaskListPanel.getParentName(value) + ": " + TaskListPanel.getTitle(value));
            this.priority.setVisible(isSelected);
            this.difficulty.setVisible(isSelected);
            this.modified.setVisible(isSelected);
            this.type.setVisible(isSelected);
            this.status.setVisible(isSelected);
            if (isSelected) {
                this.priority.setText(tr("Priority: {0}", value.priority()));
                if (value instanceof ClusteredPoint point && point.difficulty() != null) {
                    this.difficulty.setText(tr("Difficulty: {0}", point.difficulty()));
                    this.difficulty.setVisible(true);
                } else {
                    this.difficulty.setVisible(false);
                }
                this.modified.setText(tr("Modified: {0}", value.modified()));
                if (value instanceof ClusteredPoint point) {
                    this.type.setText(tr("Type: {0}", point.type()));
                } else {
                    this.type.setVisible(false);
                }
                if (value.status() != null) {
                    this.status.setText(tr("Status: {0}", value.status()));
                }
                this.status.setVisible(value.status() != null);
            }
            if (!isSelected && !cellHasFocus) {
                final var defaultColor = MRColors.statusColor(value.status());
                if (defaultColor != null) {
                    this.setBackground(defaultColor);
                }
                if (ModifiedObjects.getModifiedTask(value.id()) != null) {
                    this.setBackground(COLOR_MODIFIED.get());
                }
                if (ModifiedObjects.getLockedTask(value.id()) != null) {
                    this.setBackground(COLOR_LOCKED.get());
                }
            }
            this.updateComponentColors();
            final var bounds = this.getBounds();
            this.setBounds(bounds.x, bounds.y, bounds.width, bounds.height * this.getComponentCount());
        }
    }
}
