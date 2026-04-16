package com.codeit.mission.deokhugam.comment.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentDto(
        UUID id,
        UUID reviewId,
        UUID userId,
        String userNickName,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
