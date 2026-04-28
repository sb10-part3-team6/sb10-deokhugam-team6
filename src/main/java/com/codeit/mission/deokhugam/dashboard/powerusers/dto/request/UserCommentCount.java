package com.codeit.mission.deokhugam.dashboard.powerusers.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "사용자별 댓글 개수")
public record UserCommentCount(
    @Schema(
        description = "사용자 ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID userId,

    @Schema(
        description = "댓글 개수",
        example = "1"
    )
    long commentCount
) {

}
