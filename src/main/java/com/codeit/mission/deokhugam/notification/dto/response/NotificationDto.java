package com.codeit.mission.deokhugam.notification.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record NotificationDto(
    UUID id,
    UUID userId,
    UUID reviewId,
    String reviewContent,
    String message,
    boolean confirmed,
    Instant createdAt,
    Instant updatedAt
) {

}
