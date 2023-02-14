// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui.widgets;

import java.awt.Component;
import java.util.Objects;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 * This is a replacement for {@link javax.swing.DefaultListCellRenderer}
 *
 * @param <T> The list type
 */
public abstract class DefaultPanelListCellRenderer<T> extends JPanel implements ListCellRenderer<T>, TableCellRenderer {
    /**
     * The default label for toString rendering
     */
    private final JLabel label = new JLabel();
    /**
     * The class to use for casting
     */
    private final Class<T> clazz;

    /**
     * Create a new renderer
     *
     * @param clazz The class to use for casting
     */
    protected DefaultPanelListCellRenderer(Class<T> clazz) {
        Objects.requireNonNull(clazz);
        this.clazz = clazz;
    }

    @Override
    public final Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected,
            boolean cellHasFocus) {
        return performRendererSetup(value, isSelected, cellHasFocus);
    }

    /**
     * Set up the renderer
     *
     * @param value        The value to render
     * @param isSelected   {@code true} if the cell is selected
     * @param cellHasFocus {@code true} if the cell has focus
     * @return The component to use to render the value
     */
    private Component performRendererSetup(Object value, boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            // If we allow drag'n'drop, List.dropCellBackground/Foreground should be done here
            this.setBackground(UIManager.getColor("List.selectionBackground"));
            this.setForeground(UIManager.getColor("List.selectionForeground"));
        } else {
            this.setBackground(UIManager.getColor("List.background"));
            this.setForeground(UIManager.getColor("List.foreground"));
        }
        final Border border;
        if (cellHasFocus) {
            if (isSelected) {
                border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
            } else {
                border = UIManager.getBorder("List.focusCellHighlightBorder");
            }
        } else {
            border = new EmptyBorder(1, 1, 1, 1);
        }

        if (this.clazz.isInstance(value)) {
            this.addRenderComponents(this.clazz.cast(value), isSelected, cellHasFocus);
        }

        setBorder(border);
        this.updateComponentColors();
        return this;
    }

    /**
     * Add renderer components
     *
     * @param value        The value to use to build the renderer
     * @param isSelected   {@code true} if the cell is selected
     * @param cellHasFocus {@code true} if the cell has focus
     */
    protected void addRenderComponents(T value, boolean isSelected, boolean cellHasFocus) {
        this.label.setText(value != null ? value.toString() : "<null>");
    }

    /**
     * Update component colors
     */
    protected void updateComponentColors() {
        for (Component c : this.getComponents()) {
            c.setBackground(this.getBackground());
            c.setForeground(this.getForeground());
        }
    }

    @Override
    public final Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        final var component = performRendererSetup(value, isSelected, hasFocus);
        final var currentRowHeight = table.getRowHeight(row);
        final var newRowHeight = component.getPreferredSize().height;
        // Avoid calls to revalidate whenever possible (aka, don't set the row height if it hasn't changed)
        if (currentRowHeight != newRowHeight) {
            table.setRowHeight(row, newRowHeight);
        }
        return component;
    }
}
