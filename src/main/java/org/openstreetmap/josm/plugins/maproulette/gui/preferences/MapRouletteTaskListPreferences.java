// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.io.Serial;
import java.util.Arrays;
import java.util.Objects;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.SubPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.TabPreferenceSetting;
import org.openstreetmap.josm.gui.widgets.HideableTabbedPane;
import org.openstreetmap.josm.gui.widgets.VerticallyScrollablePanel;
import org.openstreetmap.josm.plugins.maproulette.api_caching.ChallengeCache;
import org.openstreetmap.josm.plugins.maproulette.api_caching.ProjectCache;
import org.openstreetmap.josm.plugins.maproulette.api_caching.TaskCache;
import org.openstreetmap.josm.plugins.maproulette.data.IgnoreList;
import org.openstreetmap.josm.tools.GBC;

/**
 * Task list settings
 */
public class MapRouletteTaskListPreferences implements SubPreferenceSetting {
    private JTable ignoredTaskTable;
    private JTable ignoredChallengeTable;

    @Override
    public void addGui(PreferenceTabbedPane gui) {
        final var pane = new HideableTabbedPane();
        final var ignoredTaskPanel = new JPanel(new GridBagLayout());
        final var ignoredChallengePanel = new JPanel(new GridBagLayout());
        ignoredTaskTable = buildTaskTable();
        ignoredChallengeTable = buildChallengeTable();
        final var scrollTaskTable = new VerticallyScrollablePanel(new GridBagLayout());
        final var scrollChallengeTable = new VerticallyScrollablePanel(new GridBagLayout());
        scrollTaskTable.add(ignoredTaskTable, GBC.eol().fill(GBC.BOTH));
        scrollChallengeTable.add(ignoredChallengeTable, GBC.eol().fill(GBC.BOTH));
        ignoredTaskPanel.add(ignoredTaskTable.getTableHeader(), GBC.eol().fill(GBC.HORIZONTAL));
        ignoredTaskPanel.add(scrollTaskTable, GBC.eol().fill(GBC.BOTH));
        ignoredChallengePanel.add(ignoredChallengeTable.getTableHeader(), GBC.eol().fill(GBC.HORIZONTAL));
        ignoredChallengePanel.add(scrollChallengeTable, GBC.eol().fill(GBC.BOTH));
        pane.add(tr("Ignored Tasks"), ignoredTaskPanel);
        pane.add(tr("Ignored Challenges"), ignoredChallengePanel);
        getTabPreferenceSetting(gui).addSubTab(this, tr("Task List"), pane, tr("MapRoulette Task List Settings"));
    }

    /**
     * Build a task table
     *
     * @return The task table
     */
    private static JTable buildTaskTable() {
        final var ignoredTasks = IgnoreList.ignoredTasks();
        final var table = buildTable(ignoredTasks.length, tr("Project"), tr("Challenge"), tr("Task Name"),
                tr("Task ID"), tr("Keep"));
        var row = 0;
        for (var taskId : ignoredTasks) {
            final var task = TaskCache.get(taskId);
            final var challenge = ChallengeCache.challenge(task.parentId());
            final var project = ProjectCache.get(challenge.general().parent());
            table.setValueAt(project.displayName(), row, 0);
            table.setValueAt(challenge.name(), row, 1);
            table.setValueAt(task.name(), row, 2);
            table.setValueAt(task.id(), row, 3);
            table.setValueAt(true, row, 4);
            row++;
        }
        ((NonEditableTableModel) table.getModel()).setColumnClass(3, Long.class);
        ((NonEditableTableModel) table.getModel()).setColumnEditable(4);
        ((NonEditableTableModel) table.getModel()).setColumnClass(4, Boolean.class);
        return table;
    }

    /**
     * Build a challenge table
     *
     * @return The table with ignored challenges
     */
    private static JTable buildChallengeTable() {
        final var ignoredChallenges = IgnoreList.ignoredChallenges();
        final var table = buildTable(ignoredChallenges.length, tr("Project"), tr("Challenge Name"), tr("Challenge ID"),
                tr("Keep"));
        var row = 0;
        for (var challengeId : ignoredChallenges) {
            final var challenge = ChallengeCache.challenge(challengeId);
            final var project = ProjectCache.get(challenge.general().parent());
            table.setValueAt(project.displayName(), row, 0);
            table.setValueAt(challenge.name(), row, 1);
            table.setValueAt(challenge.id(), row, 2);
            table.setValueAt(true, row, 3);
        }
        ((NonEditableTableModel) table.getModel()).setColumnClass(2, Long.class);
        ((NonEditableTableModel) table.getModel()).setColumnEditable(3);
        ((NonEditableTableModel) table.getModel()).setColumnClass(3, Boolean.class);
        return table;
    }

    @Override
    public TabPreferenceSetting getTabPreferenceSetting(PreferenceTabbedPane gui) {
        return gui.getSetting(MapRoulettePreferences.class);
    }

    /**
     * Build a table with specified headers
     *
     * @param numRows       The number of rows the model will have
     * @param columnHeaders The column headers
     * @return The built table
     */
    private static JTable buildTable(int numRows, String... columnHeaders) {
        final var table = new JTable(numRows, columnHeaders.length);
        table.setModel(new NonEditableTableModel(numRows, columnHeaders.length));
        final var columnModel = new DefaultTableColumnModel();
        table.setColumnModel(columnModel);
        for (var i = 0; i < columnHeaders.length; i++) {
            final var tableColumn = new TableColumn(i);
            tableColumn.setHeaderValue(columnHeaders[i]);
            columnModel.addColumn(tableColumn);
        }
        table.setAutoCreateRowSorter(true);
        return table;
    }

    @Override
    public boolean ok() {
        if (this.ignoredTaskTable != null) {
            for (var row = 0; row < this.ignoredTaskTable.getRowCount(); row++) {
                if (Boolean.FALSE.equals(this.ignoredTaskTable.getValueAt(row, 4))) {
                    final var obj = this.ignoredTaskTable.getValueAt(row, 3);
                    IgnoreList.unignoreTask((Long) obj);
                }
            }
        }
        if (this.ignoredChallengeTable != null) {
            for (var row = 0; row < this.ignoredChallengeTable.getRowCount(); row++) {
                if (Boolean.FALSE.equals(this.ignoredChallengeTable.getValueAt(row, 3))) {
                    final var obj = this.ignoredChallengeTable.getValueAt(row, 2);
                    IgnoreList.unignoreChallenge((Long) obj);
                }
            }
        }
        return false;
    }

    @Override
    public boolean isExpert() {
        return true;
    }

    /**
     * A table model for editing only some columns
     */
    private static class NonEditableTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 7474094285170240336L;
        private int[] editableColumns = new int[0];
        private Class<?>[] columnType = new Class<?>[0];

        public NonEditableTableModel(int numRows, int numCols) {
            super(numRows, numCols);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return Arrays.binarySearch(editableColumns, column) >= 0;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnType.length > columnIndex ? columnType[columnIndex] : super.getColumnClass(columnIndex);
        }

        void setColumnClass(int columnIndex, Class<?> clazz) {
            Objects.requireNonNull(clazz);
            if (columnType.length <= columnIndex) {
                columnType = Arrays.copyOf(columnType, columnIndex + 1);
                for (var i = 0; i < columnType.length; i++) {
                    if (columnType[i] == null) {
                        columnType[i] = super.getColumnClass(columnIndex);
                    }
                }
            }
            columnType[columnIndex] = clazz;
        }

        void setColumnEditable(int column) {
            final var location = Arrays.binarySearch(editableColumns, column);
            if (location < 0) {
                synchronized (this) {
                    editableColumns = Arrays.copyOf(editableColumns, editableColumns.length + 1);
                    editableColumns[editableColumns.length - 1] = column;
                    Arrays.sort(editableColumns);
                }
            }
        }
    }
}
