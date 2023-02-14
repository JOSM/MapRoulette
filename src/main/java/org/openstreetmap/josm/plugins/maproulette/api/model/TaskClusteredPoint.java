// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import java.time.Instant;

import org.openstreetmap.josm.plugins.maproulette.api.enums.TaskStatus;

/**
 * Commonalities between {@link Task} and {@link ClusteredPoint}
 */
public interface TaskClusteredPoint extends Locatable {
    /**
     * Get the ID of the parent object ({@link Task} -&gt; {@link Challenge} -&gt; {@link Project})
     *
     * @return The id of the parent
     */
    long parentId();

    /**
     * The time that the object was last modified
     *
     * @return The last modified time
     */
    Instant modified();

    /**
     * The status of the object
     *
     * @return The status
     */
    TaskStatus status();

    /**
     * The time that the object was mapped on
     *
     * @return The mapped on time
     */
    Instant mappedOn();

    /**
     * How long it took the user to map the task
     *
     * @return The time spent
     */
    Long completedTimeSpent();

    /**
     * The specified priority of the object
     *
     * @return The priority
     */
    int priority();

    /**
     * The bundle id for the object
     *
     * @return The bundle id
     */
    Long bundleId();

    /**
     * If the bundle is a primary bundle
     *
     * @return {@code true} if the bundle is primary
     */
    Boolean isBundlePrimary();
}
