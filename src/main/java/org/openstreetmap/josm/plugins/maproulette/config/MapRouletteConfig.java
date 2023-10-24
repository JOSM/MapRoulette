// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.config;

import java.util.Objects;

import jakarta.annotation.Nonnull;

/**
 * A class for storing MapRoulette configuration information
 *
 * @param baseUrl The base MR api URL
 */
public record MapRouletteConfig(@Nonnull String baseUrl) {
    /**
     * The config instance
     */
    private static MapRouletteConfig instance;

    /**
     * Create a new config object
     *
     * @param baseUrl The base url
     */
    public MapRouletteConfig {
        Objects.requireNonNull(baseUrl, "baseUrl");
    }

    /**
     * Get the current base URL
     *
     * @return The base url
     */
    public static String getBaseUrl() {
        return getInstance().baseUrl();
    }

    /**
     * Get the config instance
     *
     * @return The config instance
     */
    public static MapRouletteConfig getInstance() {
        return instance;
    }

    /**
     * Set the config instance
     *
     * @param instance the new config instance to use
     */
    public static void setInstance(@Nonnull MapRouletteConfig instance) {
        MapRouletteConfig.instance = instance;
    }
}
