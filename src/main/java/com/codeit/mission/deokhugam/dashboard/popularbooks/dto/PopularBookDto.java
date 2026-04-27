package com.codeit.mission.deokhugam.dashboard.popularbooks.dto;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import java.time.Instant;
import java.util.UUID;

public record PopularBookDto(
    UUID id,
    UUID bookId,
    String title,
    String author,
    PeriodType period,
    Long rank,
    double score,
    Long reviewCount,
    double rating,
    Instant createdAt
) {

}
