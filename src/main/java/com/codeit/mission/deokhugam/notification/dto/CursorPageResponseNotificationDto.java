package com.codeit.mission.deokhugam.notification.dto;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record CursorPageResponseNotificationDto(
    List<NotificationDto> content,
    String nextCursor,
    LocalDateTime nextAfter,
    int size,
    BigInteger totalElements,
    boolean hasNext
) {

}
