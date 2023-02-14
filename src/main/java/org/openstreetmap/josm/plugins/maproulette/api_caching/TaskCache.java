// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api_caching;

import org.apache.commons.jcs3.access.CacheAccess;
import org.openstreetmap.josm.data.cache.JCSCacheManager;
import org.openstreetmap.josm.plugins.maproulette.api.TaskAPI;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.plugins.maproulette.api.model.TaskClusteredPoint;
import org.openstreetmap.josm.plugins.maproulette.data.IgnoreList;

/**
 * A cache for {@link Task} objects. Use when you don't need the absolute freshest data.
 */
public final class TaskCache {
    /**
     * The cache for task objects
     */
    private static final CacheAccess<Long, Task> CACHE = JCSCacheManager.getCache("maproulette:task");

    private TaskCache() {
        // Hide constructor
    }

    /**
     * Cache results from {@link TaskAPI#get(long)}
     *
     * @param id The task id
     * @return The cached task
     */
    public static Task get(long id) {
        return CACHE.get(id, () -> TaskAPI.get(id));
    }

    /**
     * Check if we are ignoring/hiding a point
     *
     * @param point The point to check if we are ignoring
     * @return {@code true} if we are ignoring the point
     */
    public static boolean isHidden(TaskClusteredPoint point) {
        return IgnoreList.isChallengeIgnored(point.parentId()) || IgnoreList.isTaskIgnored(point.id())
                || ChallengeCache.isHidden(point.parentId());
    }
}
