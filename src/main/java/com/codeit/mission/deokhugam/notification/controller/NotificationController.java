package com.codeit.mission.deokhugam.notification.controller;

import com.codeit.mission.deokhugam.notification.dto.CursorPageResponseNotificationDto;
import com.codeit.mission.deokhugam.notification.dto.NotificationDto;
import com.codeit.mission.deokhugam.notification.dto.NotificationRequestQuery;
import com.codeit.mission.deokhugam.notification.dto.NotificationUpdateRequest;
import com.codeit.mission.deokhugam.notification.service.NotificationService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<CursorPageResponseNotificationDto> findByUserId(
        @RequestParam(name = "userId") UUID userId,
        @Valid @ParameterObject @ModelAttribute NotificationRequestQuery query) {

        return ResponseEntity.ok(notificationService.findByUserId(userId, query));

    }

    // 알림 읽음 상태 업데이트
    @PatchMapping("/{notificationId}")
    public ResponseEntity<NotificationDto> updateById(
        @PathVariable UUID notificationId,
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
        @Valid @RequestBody NotificationUpdateRequest requestDto) {

        return ResponseEntity.ok(
            notificationService.updateById(notificationId, requestUserId,
                requestDto));
    }

    // 모든 알림 읽음 처리
    @PatchMapping("/read-all")
    public ResponseEntity<Void> updateById(
        @RequestHeader("Deokhugam-Request-User-ID") UUID userId) {

        notificationService.updateByUserId(userId);
        return ResponseEntity.noContent().build();
    }

}
