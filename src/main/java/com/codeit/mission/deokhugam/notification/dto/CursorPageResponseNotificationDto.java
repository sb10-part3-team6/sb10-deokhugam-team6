package com.codeit.mission.deokhugam.notification.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record CursorPageResponseNotificationDto(
    List<NotificationDto> content,
    String nextCursor,
    Instant nextAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

}
