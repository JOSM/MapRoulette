// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui.download;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;

import org.openstreetmap.josm.actions.downloadtasks.AbstractDownloadTask;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.gui.download.IDownloadSourceType;
import org.openstreetmap.josm.plugins.maproulette.actions.downloadtasks.MapRouletteDownloadTaskBox;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * The class for showing the user that they can download MapRoulette data
 */
public class MapRouletteDownloadSource implements IDownloadSourceType {
    private static final String PREF_KEY = "download.maproulette";
    private JCheckBox cbDownloadMapRouletteData;

    @Override
    public JCheckBox getCheckBox(ChangeListener checkboxChangeListener) {
        if (cbDownloadMapRouletteData == null) {
            cbDownloadMapRouletteData = new JCheckBox(tr("MapRoulette Tasks"));
            cbDownloadMapRouletteData.setToolTipText(tr("Download MapRoulette Tasks in the selected download area"));
        }
        if (checkboxChangeListener != null) {
            cbDownloadMapRouletteData.getModel().addChangeListener(checkboxChangeListener);
        }
        return cbDownloadMapRouletteData;
    }

    @Override
    public Class<? extends AbstractDownloadTask<?>> getDownloadClass() {
        return MapRouletteDownloadTaskBox.class;
    }

    @Override
    public BooleanProperty getBooleanProperty() {
        return new BooleanProperty(PREF_KEY, false);
    }

    @Override
    public boolean isDownloadAreaTooLarge(Bounds bound) {
        return false;
    }

    @Override
    public Icon getIcon() {
        return ImageProvider.get("dialogs", "user_no_image", ImageProvider.ImageSizes.SMALLICON);
    }
}
