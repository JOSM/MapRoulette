// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import jakarta.annotation.Nullable;

/**
 * General challenge information
 *
 * @param owner           The owner of the challenge
 * @param parent          The parent project of the challenge
 * @param instruction     General challenge instructions
 * @param difficulty      The general difficulty of the challenge
 * @param blurb           The blurb for the challenge
 * @param enabled         {@code true} if the challenge is enabled
 * @param featured        {@code true} if the challenge is featured
 * @param cooperativeType the cooperative challenge type
 * @param popularity      The popularity of the challenge
 * @param checkinComment  The expected OSM comment
 * @param checkinSource   The expected OSM source
 * @param changesetUrl    Used for something. I don't know what.
 * @param virtualParents  The virtual parent projects
 * @param requiresLocal   {@code true} if the mapper should be local
 */
public record ChallengeGeneral(long owner, long parent, String instruction, int difficulty, String blurb,
                               boolean enabled, boolean featured, int cooperativeType, int popularity,
                               String checkinComment, String checkinSource, boolean changesetUrl,
                               @Nullable long[] virtualParents, boolean requiresLocal) {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ChallengeGeneral other && RecordUtils.equals(this, other);
    }

    @Override
    public int hashCode() {
        return RecordUtils.hashCode(this);
    }

    @Override
    public String toString() {
        return RecordUtils.toString(this);
    }
}
