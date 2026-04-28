package com.codeit.mission.deokhugam.dashboard.popularbooks.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "책별 리뷰 수")
public record BookReviewCount(
    @Schema(
        description = "대상 도서 ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID bookId,

    @Schema(
        description = "리뷰 개수",
        example = "1"
    )
    Long reviewCount
) {

}
