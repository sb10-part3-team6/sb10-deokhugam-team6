package com.codeit.mission.deokhugam.dashboard.reviews.dto;

import java.util.UUID;

public record ReviewCommentCount(
    UUID reviewId,
    long commentCount
) {


}
