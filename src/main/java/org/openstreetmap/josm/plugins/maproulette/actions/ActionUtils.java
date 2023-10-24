// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.actions;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.JTable;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Utils for an action
 */
final class ActionUtils {
    private ActionUtils() {
        // Hide the constructor
    }

    /**
     * Get the selected items from a component
     *
     * @param component The component to get items from
     * @return The list of selected objects
     */
    @Nonnull
    static List<?> getSelectedItems(@Nullable Component component) {
        if (component instanceof JList<?> list) {
            return list.getSelectedValuesList();
        } else if (component instanceof JTable table) {
            final var selected = table.getSelectedRows();
            final var model = table.getModel();
            final var list = new ArrayList<>(selected.length);
            for (int index : selected) {
                var i = table.getRowSorter().convertRowIndexToModel(index);
                list.add(model.getValueAt(i, 0));
            }
            return list;
        } else {
            throw new IllegalArgumentException("Cannot handle component of type "
                    + (component != null ? component.getClass().toString() : "<null>"));
        }
    }
}
