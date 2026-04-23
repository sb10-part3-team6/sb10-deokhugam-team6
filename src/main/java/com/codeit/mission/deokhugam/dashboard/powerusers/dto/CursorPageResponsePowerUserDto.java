package com.codeit.mission.deokhugam.dashboard.powerusers.dto;

import java.util.List;

public record CursorPageResponsePowerUserDto(
    List<PowerUserDto> content,
    String nextCursor,
    String nextAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

}
