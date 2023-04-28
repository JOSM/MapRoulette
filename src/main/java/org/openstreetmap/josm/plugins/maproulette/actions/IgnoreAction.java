// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.actions;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.util.List;
import java.util.Locale;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.maproulette.api.model.ClusteredPoint;
import org.openstreetmap.josm.plugins.maproulette.data.IgnoreList;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Ignore selected tasks/challenge
 */
public class IgnoreAction extends JosmAction {
    @Serial
    private static final long serialVersionUID = 2842369012780801444L;
    /**
     * The ignore type
     */
    private final IgnoreType type;

    /**
     * Create a new action object
     *
     * @param type The type of object we are ignoring
     */
    public IgnoreAction(IgnoreType type) {
        super(tr(type.getButtonText()), "dialogs/fix", tr(type.getButtonText()),
                Shortcut.registerShortcut(/* NO-SHORTCUT */ "maproulette:ignore." + type.name().toLowerCase(Locale.ROOT),
                        tr("MapRoulette: {0}", tr(type.getButtonText())), KeyEvent.CHAR_UNDEFINED, Shortcut.NONE),
                false);
        this.type = type;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final var component = ((JPopupMenu) ((JMenuItem) e.getSource()).getParent()).getInvoker();
        final List<?> objects = ActionUtils.getSelectedItems(component);
        for (var obj : objects) {
            if (obj instanceof ClusteredPoint point) {
                switch (this.type) {
                case IGNORE_TASK -> IgnoreList.ignoreTask(point.id());
                case IGNORE_CHALLENGE -> IgnoreList.ignoreChallenge(point.parentId());
                }
            }
        }
        if (component instanceof JTable table && table.getModel()instanceof AbstractTableModel model) {
            model.fireTableDataChanged();
        } else {
            throw new IllegalArgumentException("Unknown component type: " + component.getClass());
        }
    }

    /**
     * The ignore type
     */
    public enum IgnoreType {
        /* SHORTCUT("maproulette:ignore.task", "MapRoulette: Ignore Task", KeyEvent.CHAR_UNDEFINED, Shortcut.NONE) */
        IGNORE_TASK(marktr("Ignore Task")),
        /* SHORTCUT("maproulette:ignore.challenge", "MapRoulette: Ignore Challenge", KeyEvent.CHAR_UNDEFINED, Shortcut.NONE) */
        IGNORE_CHALLENGE(marktr("Ignore Challenge"));

        private final String buttonText;

        IgnoreType(String buttonText) {
            this.buttonText = buttonText;
        }

        /**
         * Get the button text for this type
         *
         * @return The text
         */
        public String getButtonText() {
            return this.buttonText;
        }
    }
}
