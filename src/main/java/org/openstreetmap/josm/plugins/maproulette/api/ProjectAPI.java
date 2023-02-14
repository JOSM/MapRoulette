// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api;

import static org.openstreetmap.josm.plugins.maproulette.config.MapRouletteConfig.getBaseUrl;

import java.io.IOException;
import java.io.UncheckedIOException;

import javax.annotation.Nonnull;

import org.openstreetmap.josm.plugins.maproulette.api.model.Project;
import org.openstreetmap.josm.plugins.maproulette.api.parsers.ProjectParser;
import org.openstreetmap.josm.plugins.maproulette.util.HttpClientUtils;

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
     */
    @Nonnull
    public static Project get(long id) {
        final var client = HttpClientUtils.get(getBaseUrl() + PROJECT + "/" + id);
        try (var inputstream = client.connect().getContent()) {
            return ProjectParser.parse(inputstream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            client.disconnect();
        }
    }
}
