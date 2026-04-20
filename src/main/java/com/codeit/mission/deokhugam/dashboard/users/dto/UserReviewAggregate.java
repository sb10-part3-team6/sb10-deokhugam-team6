package com.codeit.mission.deokhugam.dashboard.users.dto;

import java.util.UUID;

public record UserReviewAggregate(UUID userId, double reviewScoreSum) {
}
