package com.codeit.mission.deokhugam.book.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponseBookDto(
    List<BookDto> content,
    String nextCursor,
    LocalDateTime nextAfter,
    int size,
    Long totalElements,
    boolean hasNext
) {
}
