// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api_caching;

import java.io.IOException;

import org.apache.commons.jcs3.access.CacheAccess;
import org.openstreetmap.josm.data.cache.JCSCacheManager;
import org.openstreetmap.josm.plugins.maproulette.api.ProjectAPI;
import org.openstreetmap.josm.plugins.maproulette.api.model.Project;

/**
 * A cache for project objects, use when you don't need the freshest data
 */
public final class ProjectCache {
    /**
     * The cache for project objects
     */
    private static final CacheAccess<Long, Project> CACHE = JCSCacheManager.getCache("maproulette:project");

    private ProjectCache() {
        // Hide constructor
    }

    /**
     * Get a specified project
     *
     * @param id The project id to get
     * @return The project
     * @throws IOException if there was a problem communicating with the server
     */
    public static Project get(long id) throws IOException {
        if (CACHE.get(id) == null) {
            synchronized (CACHE) {
                CACHE.put(id, ProjectAPI.get(id));
            }
        }
        return CACHE.get(id);
    }
}
