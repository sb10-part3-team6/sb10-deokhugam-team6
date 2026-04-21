package com.codeit.mission.deokhugam.comment.dto.response;

import com.codeit.mission.deokhugam.comment.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CursorPageResponseCommentDto(
     List<CommentDto> content,
     String nextCursor,
     LocalDateTime nextAfter,
     int size,
     int totalElements,
     Boolean hasNext
) {
}
