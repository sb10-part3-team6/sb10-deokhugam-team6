package com.codeit.mission.deokhugam.dashboard.powerusers.dto;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import java.time.Instant;
import java.util.UUID;

public record PowerUserDto(
    UUID userId,
    String nickname,
    PeriodType period,
    Instant createdAt,
    long rank,
    double score,
    double reviewScoreSum,
    long likeCount,
    long commentCount) {
}
