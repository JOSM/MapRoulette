// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import java.time.Instant;

import javax.annotation.Nullable;

/**
 * Review information for a point
 *
 * @param reviewStatus        The current status
 * @param reviewRequestedBy   The user that requested the review
 * @param reviewedBy          The user that did the review
 * @param reviewedAt          The time that the review was done
 * @param metaReviewStatus    The meta review status
 * @param metaReviewedBy      The user that did the meta review
 * @param metaReviewedAt      The time the meta review was done
 * @param reviewStartedAt     The time that the review was started at
 * @param additionalReviewers The additional reviewers, if any
 */
public record PointReview(@Nullable Integer reviewStatus, @Nullable BaseUser reviewRequestedBy, @Nullable BaseUser reviewedBy,
                          @Nullable Instant reviewedAt, @Nullable Integer metaReviewStatus, @Nullable BaseUser metaReviewedBy,
                          @Nullable Instant metaReviewedAt, @Nullable Instant reviewStartedAt, @Nullable long[] additionalReviewers) {
}
