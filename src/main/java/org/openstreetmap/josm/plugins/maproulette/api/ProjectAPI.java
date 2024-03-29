// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api;

import static org.openstreetmap.josm.plugins.maproulette.config.MapRouletteConfig.getBaseUrl;

import java.io.IOException;

import org.openstreetmap.josm.plugins.maproulette.api.model.Project;
import org.openstreetmap.josm.plugins.maproulette.api.parsers.ProjectParser;
import org.openstreetmap.josm.plugins.maproulette.util.HttpClientUtils;

import jakarta.annotation.Nonnull;

/**
 * A class for getting data from Project APIs
 */
public final class ProjectAPI {
    /**
     * The base project path
     */
    private static final String PROJECT = "/project";

    private ProjectAPI() {
        // Hide constructor
    }

    /**
     * Get a specified project
     *
     * @param id The project to get
     * @return The parsed project object
     * @throws IOException if there was a problem communicating with the server
     */
    @Nonnull
    public static Project get(long id) throws IOException {
        final var client = HttpClientUtils.get(getBaseUrl() + PROJECT + "/" + id);
        try (var inputstream = client.connect().getContent()) {
            return ProjectParser.parse(inputstream);
        } finally {
            client.disconnect();
        }
    }
}
