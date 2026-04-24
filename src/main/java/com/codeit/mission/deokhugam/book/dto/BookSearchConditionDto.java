package com.codeit.mission.deokhugam.book.dto;

import com.codeit.mission.deokhugam.book.entity.SortDirection;
import java.time.LocalDateTime;

public record BookSearchConditionDto(
    String keyword,
    String orderBy,
    SortDirection direction,
    Object cursor,
    LocalDateTime after,
    int limit
) {

}
