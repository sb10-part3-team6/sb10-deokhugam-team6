package com.codeit.mission.deokhugam.comment.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentFindAllRequest(
        @NotNull
        UUID reviewId,
        String direction,
        String cursor,
        LocalDateTime after,
        int limit
) {
}
