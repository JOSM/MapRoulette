// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.io.Serial;
import java.util.Arrays;
import java.util.Objects;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A class for showing tag changes
 */
public class TagChangeTable extends JTable {
    /**
     * The serial id for the table
     */
    @Serial
    private static final long serialVersionUID = 7522835248836720909L;

    /**
     * Create a new table
     */
    public TagChangeTable() {
        this(true);
    }

    /**
     * Create a new table
     *
     * @param isEditable {@code true} if you want the user to use the keep column
     */
    public TagChangeTable(boolean isEditable) {
        super(new TagModel(isEditable));
        this.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        this.setDefaultRenderer(String.class, new CellRenderer());
    }

    /**
     * A cell renderer for tag changes
     */
    private static class CellRenderer extends DefaultTableCellRenderer {
        @Serial
        private static final long serialVersionUID = -4282059350134834092L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            final var component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            // Colors from TwoColumnDiff.Item.DiffItemType
            if (!isSelected && !hasFocus) {
                final var old = table.getValueAt(row, 1);
                final var newObj = table.getValueAt(row, 2);
                if (old != null && newObj == null) {
                    // DiffItemType.DELETED
                    component.setBackground(new Color(255, 197, 197));
                } else if (old == null && newObj != null) {
                    // DiffItemType.INSERTED
                    component.setBackground(new Color(0xDD, 0xFF, 0xDD));
                } else if (old == null) {
                    // DiffItemType.EMPTY
                    component.setBackground(new Color(234, 234, 234));
                } else if (!Objects.equals(old, newObj)) {
                    // DiffItemType.CHANGED
                    component.setBackground(new Color(255, 234, 213));
                } else {
                    // DiffItemType.SAME
                    component.setBackground(UIManager.getColor("Panel.background"));
                } // DiffItemType.REVERSED did not map
            }
            return component;
        }
    }

    /**
     * The tag model for changed values
     */
    private static class TagModel extends AbstractTableModel {
        /**
         * The serial id
         */
        @Serial
        private static final long serialVersionUID = -7536950115021624629L;
        /**
         * {@code true} if the user can make edits
         */
        private final boolean isEditable;
        /**
         * The tag values
         */
        private String[] tags = new String[0];
        /**
         * The previous value for the tag
         */
        private String[] oldValues = tags;
        /**
         * The new value for the tag
         */
        private String[] newValues = oldValues;
        /**
         * Keep the change
         */
        private boolean[] keep = new boolean[0];

        /**
         * Create a new model
         *
         * @param isEditable {@code true} if the model should be editable
         */
        public TagModel(boolean isEditable) {
            this.isEditable = isEditable;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
            case 0 -> tr("Tag");
            case 1 -> tr("Old value");
            case 2 -> tr("New value");
            case 3 -> tr("Keep");
            default -> super.getColumnName(column);
            };
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
            case 0, 1, 2 -> String.class;
            case 3 -> Boolean.class;
            default -> super.getColumnClass(columnIndex);
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return switch (columnIndex) {
            case 0, 1, 2 -> false;
            case 3 -> true;
            default -> super.isCellEditable(rowIndex, columnIndex);
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return switch (columnIndex) {
            case 0 -> tags[rowIndex];
            case 1 -> oldValues[rowIndex];
            case 2 -> newValues[rowIndex];
            case 3 -> keep[rowIndex];
            default -> null;
            };
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            super.setValueAt(aValue, rowIndex, columnIndex);
            if ((rowIndex <= getRowCount() && rowIndex >= 0 && columnIndex >= 0 && columnIndex <= getColumnCount())) {
                final var isString = aValue instanceof String || (aValue == null && rowIndex < getRowCount());
                expandArrays(rowIndex + 1);
                if (columnIndex == 0 && isString) {
                    tags[rowIndex] = (String) aValue;
                } else if (columnIndex == 1 && isString) {
                    oldValues[rowIndex] = (String) aValue;
                } else if (columnIndex == 2 && isString) {
                    newValues[rowIndex] = (String) aValue;
                } else if (columnIndex == 3 && aValue instanceof Boolean bool) {
                    keep[rowIndex] = bool;
                } else if (columnIndex == 3 && aValue == null) {
                    keep[rowIndex] = true; // Reset to the default
                }
                this.fireTableCellUpdated(rowIndex, columnIndex);
            }
        }

        @Override
        public int getRowCount() {
            return tags.length;
        }

        @Override
        public int getColumnCount() {
            return this.isEditable ? 4 : 3;
        }

        /**
         * Expand the backing storage arrays
         *
         * @param newSize The expected new size
         */
        private void expandArrays(int newSize) {
            if (tags.length > newSize) {
                return;
            }
            tags = Arrays.copyOf(tags, newSize);
            oldValues = Arrays.copyOf(oldValues, newSize);
            newValues = Arrays.copyOf(newValues, newSize);
            keep = Arrays.copyOf(keep, newSize);
            keep[newSize - 1] = true;
            this.fireTableRowsInserted(newSize, newSize);
        }
    }
}
