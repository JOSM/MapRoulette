// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

/**
 * A bundle of tasks
 *
 * @param id      The id of the bundle
 * @param name    The name of the bundle
 * @param ownerId The user who owns the bundle
 * @param taskIds The ids of the tasks
 * @param tasks   The tasks
 */
public record TaskBundle(long id, String name, long ownerId, long[] taskIds,
                         Task[] tasks) implements Identifier {
}
