package com.codeit.mission.deokhugam.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Schema(description = "리뷰 좋아요 응답")
@Builder
public record ReviewLikeDto(
    @Schema(
        description = "리뷰 ID",
        example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    )
    UUID reviewId,

    @Schema(
        description = "요청자 ID",
        example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    )
    UUID userId,

    @Schema(
        description = "요청자의 좋아요 여부",
        example = "true"
    )
    Boolean liked
) {

}
