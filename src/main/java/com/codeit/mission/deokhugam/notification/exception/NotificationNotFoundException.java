package com.codeit.mission.deokhugam.notification.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class NotificationNotFoundException extends DeokhugamException {

    public NotificationNotFoundException(UUID notificationId) {
        super(ErrorCode.NOTIFICATION_NOT_FOUND, Map.of("notificationId", notificationId));
    }
}
