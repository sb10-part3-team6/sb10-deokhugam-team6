package com.codeit.mission.deokhugam.dashboard.popularbooks.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "책별 지수")
public record PopularBookStat(
    @Schema(
        description = "도서 ID",
        example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    )
    UUID bookId,

    @Schema(
        description = "리뷰 개수",
        example = "1"
    )
    Long reviewCount,

    @Schema(
        description = "리뷰 평균 평점",
        example = "1.0"
    )
    double reviewAvgRating
) {

}
