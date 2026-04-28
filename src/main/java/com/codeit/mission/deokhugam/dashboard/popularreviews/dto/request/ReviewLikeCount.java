package com.codeit.mission.deokhugam.dashboard.popularreviews.dto;

import java.util.UUID;

public record ReviewLikeCount(
    UUID reviewId,
    long likeCount
) {

}
