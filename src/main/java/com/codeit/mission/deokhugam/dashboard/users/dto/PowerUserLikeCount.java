package com.codeit.mission.deokhugam.dashboard.users.dto;

import com.codeit.mission.deokhugam.dashboard.reviews.dto.ReviewStat;
import java.util.Map;
import java.util.UUID;

public record PowerUserLikeCount(UUID userId, long likeCount) {
}
