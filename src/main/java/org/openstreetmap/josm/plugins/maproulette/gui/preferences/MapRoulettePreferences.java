// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.gui.preferences.ExtensibleTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.SubPreferenceSetting;

/**
 * The base preferences class for the MR plugin
 */
public class MapRoulettePreferences extends ExtensibleTabPreferenceSetting {
    /**
     * The child settings
     */
    private final List<SubPreferenceSetting> subPreferenceSettingList = new ArrayList<>();

    /**
     * Create a new preference object
     */
    public MapRoulettePreferences() {
        super("dialogs/user_no_image", tr("MapRoulette"), tr("MapRoulette Preferences"));
        this.subPreferenceSettingList.add(new MapRouletteServerPreference());
        this.subPreferenceSettingList.add(new MapRouletteTaskListPreferences());
        this.subPreferenceSettingList.add(new MapRouletteTaskPreference());
    }

    @Override
    public void addGui(PreferenceTabbedPane gui) {
        super.addGui(gui);
        this.subPreferenceSettingList.forEach(setting -> setting.addGui(gui));
    }

    @Override
    public boolean ok() {
        var ok = false;
        for (SubPreferenceSetting setting : this.subPreferenceSettingList) {
            ok |= setting.ok();
        }
        return ok;
    }
}
