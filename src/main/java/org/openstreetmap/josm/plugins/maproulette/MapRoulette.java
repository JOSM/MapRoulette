// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette;

import org.openstreetmap.josm.actions.UploadAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.download.OSMDownloadSource;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.maproulette.actions.downloadtasks.MapRouletteDownloadTask;
import org.openstreetmap.josm.plugins.maproulette.api.TaskAPI;
import org.openstreetmap.josm.plugins.maproulette.gui.ModifiedObjects;
import org.openstreetmap.josm.plugins.maproulette.gui.TaskListPanel;
import org.openstreetmap.josm.plugins.maproulette.gui.download.MapRouletteDownloadSource;
import org.openstreetmap.josm.plugins.maproulette.gui.preferences.MapRoulettePreferences;
import org.openstreetmap.josm.plugins.maproulette.io.upload.EarlyUploadHook;
import org.openstreetmap.josm.plugins.maproulette.io.upload.LateUploadHook;

/**
 * The POJO entry point
 */
public class MapRoulette extends Plugin {
    /**
     * Creates the plugin
     *
     * @param info the plugin information describing the plugin.
     */
    public MapRoulette(PluginInformation info) {
        super(info);
        this.getPreferenceSetting().ok();
        UploadAction.registerUploadHook(new EarlyUploadHook());
        UploadAction.registerUploadHook(new LateUploadHook(), true);
        OSMDownloadSource.addDownloadType(new MapRouletteDownloadSource());
        MainApplication.getMenu().openLocation.addDownloadTaskClass(MapRouletteDownloadTask.class);
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new MapRoulettePreferences();
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        super.mapFrameInitialized(oldFrame, newFrame);
        if (newFrame != null) {
            newFrame.addToggleDialog(new TaskListPanel());
        } else {
            for (var task : ModifiedObjects.getLockedTasks()) {
                TaskAPI.release(task.id());
            }
        }
    }
}
