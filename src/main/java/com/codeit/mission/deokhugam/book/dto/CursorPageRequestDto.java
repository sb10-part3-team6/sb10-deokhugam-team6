package com.codeit.mission.deokhugam.book.dto;

import com.codeit.mission.deokhugam.book.entity.SortDirection;

import java.time.LocalDateTime;

public record CursorPageRequestDto(
        String keyword,
        String orderBy,
        SortDirection direction,
        String cursor,
        LocalDateTime after,
        int limit
) {
}
