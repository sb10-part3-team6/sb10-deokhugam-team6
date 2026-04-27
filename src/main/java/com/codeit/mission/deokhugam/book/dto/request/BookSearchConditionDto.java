package com.codeit.mission.deokhugam.book.dto.request;

import com.codeit.mission.deokhugam.book.entity.SortDirection;
import java.time.Instant;

public record BookSearchConditionDto(
    String keyword,
    String orderBy,
    SortDirection direction,
    Object cursor,
    Instant after,
    int limit
) {

}
