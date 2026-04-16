package com.codeit.mission.deokhugam.comment.dto.request;

import java.util.UUID;

public record CommentCreateRequest(
        UUID reviewId,
        UUID userId,
        String content
) {}
