package com.codeit.mission.deokhugam.dashboard.popularbooks.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "책별 평균 리뷰 점수")
public record BookReviewAvgRating(
    @Schema(
        description = "대상 도서 ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID bookId,
    @Schema(
        description = "평균 리뷰",
        example = "5.5"
    )
    double avgRating
) {

}
