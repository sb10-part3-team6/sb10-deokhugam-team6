package com.codeit.mission.deokhugam.dashboard.popularreviews.dto;

import java.util.UUID;

public record ReviewCommentCount(
    UUID reviewId,
    long commentCount
) {


}
