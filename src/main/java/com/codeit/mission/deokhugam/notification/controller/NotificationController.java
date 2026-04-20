package com.codeit.mission.deokhugam.notification.controller;

import com.codeit.mission.deokhugam.notification.dto.CursorPageResponseNotificationDto;
import com.codeit.mission.deokhugam.notification.dto.NotificationRequestQuery;
import com.codeit.mission.deokhugam.notification.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
        @ParameterObject @ModelAttribute NotificationRequestQuery query) {

        return ResponseEntity.ok(notificationService.findByUserId(userId, query));

    }

}
