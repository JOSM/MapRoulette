// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.downloadtasks.DownloadParams;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.plugins.maproulette.actions.downloadtasks.MapRouletteDownloadTask;
import org.openstreetmap.josm.plugins.maproulette.api.model.TaskClusteredPoint;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Download additional nearby tasks for that challenge
 */
public class DownloadTasks extends JosmAction {
    /**
     * Create a new action for downloading tasks
     */
    public DownloadTasks() {
        super(tr("Download additional tasks"), "download", tr("Download additional tasks from challenge"),
                Shortcut.registerShortcut("maproulette:download_additional_tasks_from_challenge",
                        tr("MapRoulette: Download additional tasks from challenge"), KeyEvent.CHAR_UNDEFINED,
                        Shortcut.NONE),
                false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final var component = ((JPopupMenu) ((JMenuItem) e.getSource()).getParent()).getInvoker();
        final List<?> objects = ActionUtils.getSelectedItems(component);
        final var locations = new ArrayDeque<TaskClusteredPoint>(objects.size());
        for (var obj : objects) {
            if (obj instanceof TaskClusteredPoint t) {
                locations.add(t);
            }
        }
        while (!locations.isEmpty()) {
            final var t = locations.pop();
            new MapRouletteDownloadTask(t.parentId(), t.id()).loadUrl(new DownloadParams(), "",
                    NullProgressMonitor.INSTANCE);
        }
    }
}
