// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.util;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.plugins.maproulette.config.MapRouletteConfig.getBaseUrl;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.data.UserIdentityManager;
import org.openstreetmap.josm.gui.ConditionalOptionPaneUtil;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.server.ServerAccessPreference;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.plugins.maproulette.api.UnauthorizedException;
import org.openstreetmap.josm.tools.ExceptionUtil;

/**
 * This is a more generic class for explaining exceptions, see {@link org.openstreetmap.josm.gui.ExceptionDialogUtil}
 */
public final class ExceptionDialogUtil {
    private ExceptionDialogUtil() {
        // Hide cosntructor
    }

    /**
     * Explain exceptions
     * @param exception The exception to explain to the user
     */
    public static void explainException(Exception exception) {
        if (exception instanceof UnauthorizedException) {
            OsmPreferenceUtils.clearCachedKey();
            final var message = UserIdentityManager.getInstance().isAnonymous()
                    ? tr("Please log in to OpenStreetMap in JOSM")
                    : tr("Please log in to the MapRoulette instance at least once: {0}\n NOTE: you may need to reset your MapRoulette API key",
                            getBaseUrl());
            GuiHelper.runInEDT(() -> {
                ConditionalOptionPaneUtil.showMessageDialog("maproulette.user.not.logged.in",
                        MainApplication.getMainFrame(), message, message, JOptionPane.ERROR_MESSAGE);
                if (UserIdentityManager.getInstance().isAnonymous()) {
                    final var p = new PreferenceDialog(MainApplication.getMainFrame());
                    SwingUtilities.invokeLater(() -> p.selectPreferencesTabByClass(ServerAccessPreference.class));
                    p.setVisible(true);
                }
            });
            showErrorDialog(message, tr("Unauthorized"), null);
        } else if (exception instanceof SocketException socketException) {
            final var message = tr("<html>Failed to open a connection to the remote server<br>" + "''{0}''.<br>"
                    + "Please check your internet connection.", socketException.getMessage()) + "</html>";
            showErrorDialog(message, tr("Network exception"), ht("/ErrorMessages#NestedSocketException"));
        } else if (exception instanceof UnknownHostException unknownHostException) {
            showErrorDialog(unknownHostException.getMessage(), tr("Unknown host"), ht("/ErrorMessages#UnknownHost"));
        } else if (exception instanceof IOException) {
            showErrorDialog(ExceptionUtil.explainException(exception), tr("IO Exception"),
                    ht("/ErrorMessages#NestedIOException"));
        } else {
            org.openstreetmap.josm.gui.ExceptionDialogUtil.explainException(exception);
        }
    }

    /**
     * Show an error dialog
     * @param msg The message to show
     * @param title The dialog title
     * @param helpTopic The help topic, if present
     * @return See {@link HelpAwareOptionPane#showOptionDialog(Component, Object, String, int, String)}
     */
    private static int showErrorDialog(String msg, String title, String helpTopic) {
        return HelpAwareOptionPane.showOptionDialog(MainApplication.getMainFrame(), msg, title,
                JOptionPane.ERROR_MESSAGE, helpTopic);
    }
}
