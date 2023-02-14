// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openstreetmap.josm.data.UserIdentityManager;
import org.openstreetmap.josm.data.osm.UserInfo;
import org.openstreetmap.josm.io.OsmConnection;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.testutils.annotations.BasicPreferences;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@BasicPreferences
@ExtendWith(OsmUser.Extension.class)
public @interface OsmUser {
    class Extension implements BeforeEachCallback, AfterEachCallback {
        @Override
        public void afterEach(ExtensionContext context) {
            UserIdentityManager.getInstance().setAnonymous();
            OsmConnection.setOAuthAccessTokenFetcher(url -> {
                throw new IllegalStateException("Use @OSMUser");
            });
            Config.getPref().put("osm-server.auth-method", null);
        }

        @Override
        public void beforeEach(ExtensionContext context) {
            final var info = new UserInfo();
            info.setId(2078753);
            UserIdentityManager.getInstance().setFullyIdentified("vorpalblade", info);
            Config.getPref().put("osm-server.auth-method", "basic");
            Config.getPref().put("osm-server.username", "vorpalblade");
            Config.getPref().put("osm-server.password", "notAPassword");
        }
    }
}
