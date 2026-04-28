package com.codeit.mission.deokhugam.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "수정할 알림 상태 정보")
public record NotificationUpdateRequest(
    @Schema(
        description = "상태 정보",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    Boolean confirmed
) {

}
