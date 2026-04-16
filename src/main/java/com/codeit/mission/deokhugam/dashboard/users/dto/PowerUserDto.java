package com.codeit.mission.deokhugam.dashboard.users.dto;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import java.time.LocalDateTime;
import java.util.UUID;

public record PowerUserDto(
    UUID userId,
    String nickname,
    PeriodType period,
    LocalDateTime createdAt,
    long rank,
    double score,
    double reviewScoreSum,
    long likeCount,
    long commentCount) {
}
