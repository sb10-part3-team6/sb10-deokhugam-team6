package com.codeit.mission.deokhugam.dashboard.reviews.dto;

import java.util.UUID;

public record ReviewLikeCount(
    UUID reviewId,
    long likeCount
) {

}
