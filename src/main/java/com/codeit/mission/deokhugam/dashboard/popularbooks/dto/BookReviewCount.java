package com.codeit.mission.deokhugam.dashboard.popularbooks.dto;

import java.util.UUID;

// 책 별 리뷰 수
public record BookReviewCount(
    UUID bookId,
    Long reviewCount
) {
}
