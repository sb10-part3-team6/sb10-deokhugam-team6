package com.codeit.mission.deokhugam.dashboard.popularbooks.dto;

import java.util.UUID;

// 책 별 리뷰들의 평균 점수
public record BookReviewAvgRating(
    UUID bookId,
    double avgRating
) {

}
