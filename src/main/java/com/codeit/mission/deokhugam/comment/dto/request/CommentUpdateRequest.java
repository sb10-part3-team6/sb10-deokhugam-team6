package com.codeit.mission.deokhugam.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "수정할 댓글 정보")
public record CommentUpdateRequest(
    @Schema(
        description = "내용",
        example = "string",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Size(max = 500, message = "댓글 내용은 500자 이하로 입력해주세요.")
    String content
) {

}
