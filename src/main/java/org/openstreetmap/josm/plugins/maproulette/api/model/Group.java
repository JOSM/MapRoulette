// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import java.time.Instant;

/**
 * A group
 *
 * @param id          The group id
 * @param name        The group name
 * @param description The group description
 * @param avatarURL   The avatar url for the group
 * @param groupType   The group type
 * @param created     The time that the group was created
 * @param modified    The last time that the group was modified
 */
public record Group(long id, String name, String description, String avatarURL, int groupType, Instant created,
                    Instant modified) implements Identifier {
}
