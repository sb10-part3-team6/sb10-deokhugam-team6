package com.codeit.mission.deokhugam.dashboard.powerusers.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "사용자별 지수")
public record UserStat(
    @Schema(
        description = "사용자 ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID userId,

    @Schema(
        description = "리뷰 인기도 점수 합계",
        example = "1.0"
    )
    double reviewScoreSum,

    @Schema(
        description = "좋아요 개수",
        example = "1"
    )
    long likeCount,

    @Schema(
        description = "댓글 개수",
        example = "1"
    )
    long commentCount,

    @Schema(
        description = "인기도 점수",
        example = "1.0"
    )
    double score
) {

}
