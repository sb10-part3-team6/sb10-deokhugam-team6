package com.codeit.mission.deokhugam.dashboard.popularbooks.dto;

import java.util.UUID;

// 책 별 지수
public record PopularBookStat(
    UUID bookId,
    Long reviewCount,
    double reviewAvgRating
) {

}
