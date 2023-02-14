// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.util;

import static org.openstreetmap.josm.plugins.maproulette.config.MapRouletteConfig.getBaseUrl;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.josm.data.UserIdentityManager;
import org.openstreetmap.josm.data.preferences.ListProperty;
import org.openstreetmap.josm.gui.progress.swing.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.io.OsmServerUserPreferencesReader;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.maproulette.api.UnauthorizedException;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.JosmRuntimeException;
import org.openstreetmap.josm.tools.Utils;

/**
 * Get preference information
 */
public final class OsmPreferenceUtils {
    /**
     * Prevent instantiation of the utils
     */
    private OsmPreferenceUtils() {
        // Hide constructor
    }

    /**
     * Get the MapRoulette key for the current user
     *
     * @return The key
     * @throws UnauthorizedException If the user is not logged in to either OSM through JOSM <i>or</i> has not logged in to MapRoulette.
     */
    static String getMapRouletteApiKey() throws UnauthorizedException {
        final var user = UserIdentityManager.getInstance().getUserInfo();
        final var userList = new ListProperty("maproulette.openstreetmap.users", Collections.emptyList());
        if (user == null) {
            // Right now JOSM doesn't support multiple users, so just wipe everything. If JOSM ever supports multiple users
            for (var userId : userList.get()) {
                final var preferenceKey = "maproulette.openstreetmap." + getBaseUrl() + userId;
                Config.getPref().put(preferenceKey, null);
            }
            throw new UnauthorizedException("User is not logged in");
        }
        final var preferenceKey = "maproulette.openstreetmap." + getBaseUrl() + "." + user.getId();
        final var possibleApiKey = Config.getPref().get(preferenceKey);
        if (!Utils.isBlank(possibleApiKey) && !"Couldn't authenticate you".equals(possibleApiKey)) {
            return possibleApiKey;
        }
        final var osmServerKey = Config.getPref().get("maproulette.openstreetmap" + getBaseUrl() + ".api_key",
                "maproulette_apikey_v2");
        final var reader = new OsmServerUserPreferencesReader();
        final var monitor = new PleaseWaitProgressMonitor(tr("Fetching OpenStreetMap User Preferences"));
        try {
            final var key = reader.fetchUserPreferences(monitor, tr("Getting MapRoulette API Key"))
                    .getOrDefault(osmServerKey, null);
            List<String> userIds = new ArrayList<>(userList.get());
            if (!userIds.contains(String.valueOf(user.getId()))) {
                userIds.add(String.valueOf(user.getId()));
                userList.put(userIds);
            }
            Config.getPref().put(preferenceKey, key);
            return key;
        } catch (OsmTransferException e) {
            throw new JosmRuntimeException(e);
        } finally {
            monitor.close();
        }
    }
}
