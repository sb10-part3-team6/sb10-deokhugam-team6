package com.codeit.mission.deokhugam.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "댓글 등록 정보")
public record CommentCreateRequest(
    @Schema(
        description = "대상 리뷰 ID",
        example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    UUID reviewId,

    @Schema(
        description = "댓글 작성자 ID",
        example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    UUID userId,

    @Schema(
        description = "내용",
        example = "string",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    @Size(max = 500, message = "댓글 내용은 500자 이하로 입력해주세요.")
    String content
) {

}
