// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import java.time.Instant;

import jakarta.annotation.Nullable;

/**
 * Task review information
 *
 * @param reviewStatus        The current review status
 * @param reviewRequestedBy   The user who requested the review
 * @param reviewedBy          The user who reviewed the task
 * @param reviewedAt          The time the task was reviewed
 * @param metaReviewedBy      The user who did a meta review
 * @param metaReviewStatus    The meta review status
 * @param metaReviewedAt      The time that the meta review happened at
 * @param reviewStartedAt     The time that the review started at
 * @param reviewClaimedBy     The user who claimed the review
 * @param reviewClaimedAt     The time that the review was claimed
 * @param additionalReviewers Any additional reviewers
 */
public record TaskReviewFields(@Nullable Integer reviewStatus, @Nullable Long reviewRequestedBy, @Nullable Long reviewedBy,
                               @Nullable Instant reviewedAt, @Nullable Long metaReviewedBy, @Nullable Integer metaReviewStatus,
                               @Nullable Instant metaReviewedAt, @Nullable Instant reviewStartedAt, @Nullable Long reviewClaimedBy,
                               @Nullable Instant reviewClaimedAt, long... additionalReviewers) {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof TaskReviewFields other && RecordUtils.equals(this, other);
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
