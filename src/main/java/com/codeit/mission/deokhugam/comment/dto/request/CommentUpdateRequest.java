package com.codeit.mission.deokhugam.comment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

public record CommentUpdateRequest(
        @NotNull
        @Size(max = 500, message = "댓글 내용은 500자 이하로 입력해주세요.")
        String content
) {
}
