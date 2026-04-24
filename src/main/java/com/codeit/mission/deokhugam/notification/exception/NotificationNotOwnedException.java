package com.codeit.mission.deokhugam.notification.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

public class NotificationNotOwnedException extends DeokhugamException {

    public NotificationNotOwnedException() {
        super(ErrorCode.NOTIFICATION_NOT_OWNED);
    }
}
