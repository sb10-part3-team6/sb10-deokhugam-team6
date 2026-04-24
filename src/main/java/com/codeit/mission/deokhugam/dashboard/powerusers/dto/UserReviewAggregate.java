package com.codeit.mission.deokhugam.dashboard.powerusers.dto;

import java.util.UUID;

public record UserReviewAggregate(UUID userId, double reviewScoreSum) {
}
