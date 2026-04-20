package com.codeit.mission.deokhugam.notification.dto;

import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record NotificationRequestQuery(
    DirectionEnum direction, // note: 공통 enum으로 빼도 될듯
    String cursor,
    LocalDateTime after,
    Integer limit

) {

    public int getLimitOrDefault() {
        return limit == null ? 20 : limit;
    }
}
