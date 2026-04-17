package com.codeit.mission.deokhugam.comment.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentFindAllRequest(
        UUID reviewId,
        String direction,
        String cursor,
        LocalDateTime after,
        int limit
) {
}
