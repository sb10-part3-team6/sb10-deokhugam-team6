package com.codeit.mission.deokhugam.notification.dto;

import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import lombok.Builder;

@Builder
public record NotificationRequestQuery(
    DirectionEnum direction, // note: 공통 enum으로 빼도 될듯
    Instant cursor,
    Instant after,

    @Min(value = 1, message = "Page size must not be less than one")
    @Max(value = 100, message = "Page size must be no more than 100")
    Integer limit

) {

    private static final int DEFAULT_LIMIT = 20;

    public int getLimitOrDefault() {
        return limit == null ? DEFAULT_LIMIT : limit;
    }
}
