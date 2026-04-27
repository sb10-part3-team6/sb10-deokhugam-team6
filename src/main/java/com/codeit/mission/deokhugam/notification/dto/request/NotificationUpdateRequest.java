package com.codeit.mission.deokhugam.notification.dto.request;

import jakarta.validation.constraints.NotNull;

public record NotificationUpdateRequest(
    @NotNull
    Boolean confirmed
) {

}
