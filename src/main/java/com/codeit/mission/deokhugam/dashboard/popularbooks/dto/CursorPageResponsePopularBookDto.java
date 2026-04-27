package com.codeit.mission.deokhugam.dashboard.popularbooks.dto;

import java.util.List;

public record CursorPageResponsePopularBookDto(
    List<PopularBookDto> content,
    String nextCursor,
    String nextAfter,
    int size,
    long totalElements,
    boolean hasNext
)
{

}
