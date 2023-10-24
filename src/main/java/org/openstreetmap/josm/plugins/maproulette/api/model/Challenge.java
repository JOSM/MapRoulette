// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import java.time.Instant;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * The base challenge record
 *
 * @param id                   The id of the challenge
 * @param name                 The name of the challenge
 * @param created              The time that the challenge was created
 * @param modified             The last time that the challenge was modified
 * @param description          The challenge description
 * @param deleted              {@code true} if the challenge is deleted
 * @param infoLink             A link for more information
 * @param general              The general challenge information
 * @param creation             The challenge creation information (only used for new challenges)
 * @param priority             The default priority of the challenge tasks
 * @param extra                Any extra information
 * @param status               The current status of the challenge
 * @param statusMessage        The status message
 * @param lastTaskRefresh      The last time that the tasks were refreshed
 * @param dataOriginDate       The last time that the data for the tasks were refreshed
 * @param location             The location of the challenge
 * @param bounding             The bounds of the challenge
 * @param completionPercentage The current completion percentage
 * @param tasksRemaining       The remaining task count
 */
public record Challenge(long id, String name, Instant created, Instant modified,
                        @Nullable String description, boolean deleted, @Nullable String infoLink,
                        @Nonnull ChallengeGeneral general, @Nonnull ChallengeCreation creation,
                        @Nonnull ChallengePriority priority, @Nonnull ChallengeExtra extra,
                        @Nullable Integer status, @Nullable String statusMessage, @Nullable Instant lastTaskRefresh,
                        @Nullable Instant dataOriginDate, @Nullable Point location, @Nullable String bounding,
                        @Nullable Integer completionPercentage, @Nullable Integer tasksRemaining) implements Locatable {
}
