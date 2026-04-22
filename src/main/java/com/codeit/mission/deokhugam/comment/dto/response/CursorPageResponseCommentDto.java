package com.codeit.mission.deokhugam.comment.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponseCommentDto(
     List<CommentDto> content,
     String nextCursor,
     LocalDateTime nextAfter,
     int size,
     int totalElements,
     boolean hasNext
) {
}
