// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api_caching;

import java.io.IOException;

import org.apache.commons.jcs3.access.CacheAccess;
import org.openstreetmap.josm.data.cache.JCSCacheManager;
import org.openstreetmap.josm.plugins.maproulette.api.ChallengeAPI;
import org.openstreetmap.josm.plugins.maproulette.api.model.Challenge;
import org.openstreetmap.josm.tools.Logging;

/**
 * A cache for challenge objects, which don't change often. Use this if you don't need the absolute freshest data.
 */
public final class ChallengeCache {
    /**
     * The cache for challenge objects
     */
    private static final CacheAccess<Long, Challenge> CACHE = JCSCacheManager.getCache("maproulette:challenge");

    /**
     * Prevent instantiation
     */
    private ChallengeCache() {
        // Hide constructor
    }

    /**
     * Check if a challenge is hidden
     *
     * @param id The id of the challenge to check
     * @return {@code true} if the challenge is hidden and its tasks should not be shown
     */
    public static boolean isHidden(long id) {
        try {
            final var challenge = challenge(id);
            return challenge.deleted();
        } catch (IOException ioException) {
            Logging.trace(ioException);
            return false;
        }
    }

    /**
     * Cache results from {@link ChallengeAPI#challenge(long)}
     *
     * @param id The challenge id
     * @return The cached challenge
     * @throws IOException if there was a problem communicating with the server
     */
    public static Challenge challenge(long id) throws IOException {
        if (CACHE.get(id) != null) {
            return CACHE.get(id);
        }
        synchronized (CACHE) {
            if (CACHE.get(id) == null) {
                final var challenge = ChallengeAPI.challenge(id);
                CACHE.put(id, challenge);
            }
        }
        return CACHE.get(id);
    }
}
