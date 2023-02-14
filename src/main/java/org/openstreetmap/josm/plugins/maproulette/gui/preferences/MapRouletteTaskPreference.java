// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JPanel;

import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.SubPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.TabPreferenceSetting;

/**
 * MR Task preferences
 */
public class MapRouletteTaskPreference implements SubPreferenceSetting {
    @Override
    public void addGui(PreferenceTabbedPane gui) {
        getTabPreferenceSetting(gui).addSubTab(this, tr("Task Preferences"), new JPanel(),
                tr("Future home of Task Preferences"));
    }

    @Override
    public TabPreferenceSetting getTabPreferenceSetting(PreferenceTabbedPane gui) {
        return gui.getSetting(MapRoulettePreferences.class);
    }

    @Override
    public boolean ok() {
        return false;
    }

    @Override
    public boolean isExpert() {
        return true;
    }
}
