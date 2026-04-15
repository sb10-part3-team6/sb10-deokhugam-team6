package com.codeit.mission.deokhugam.notification.dto;

import java.time.LocalDateTime;
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
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
