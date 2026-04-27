package com.codeit.mission.deokhugam.book.dto.response;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseBookDto(
    List<BookDto> content,
    String nextCursor,
    Instant nextAfter,
    int size,
    Long totalElements,
    boolean hasNext
) {

}
