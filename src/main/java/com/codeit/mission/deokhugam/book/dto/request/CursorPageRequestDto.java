package com.codeit.mission.deokhugam.book.dto.request;

import com.codeit.mission.deokhugam.book.entity.SortDirection;

public record CursorPageRequestDto(
    String keyword,
    String orderBy,
    SortDirection direction,
    String cursor,
    String after,
    int limit
) {

}
