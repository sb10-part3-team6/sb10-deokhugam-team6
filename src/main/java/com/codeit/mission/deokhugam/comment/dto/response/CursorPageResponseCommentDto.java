package com.codeit.mission.deokhugam.comment.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public class CursorPageResponseCommentDto<T> {
    List<CommentDto> content;
    String nextCursor;
    LocalDateTime nextAfter;
    int size;
    long totalElements;
    boolean hasNext;
}
