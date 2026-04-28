package com.codeit.mission.deokhugam.dashboard.popularreviews.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "리뷰별 댓글 개수")
public record ReviewCommentCount(
    @Schema(
        description = "리뷰 ID",
        example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    )
    UUID reviewId,

    @Schema(
        description = "댓글 개수",
        example = "1"
    )
    long commentCount
) {


}
