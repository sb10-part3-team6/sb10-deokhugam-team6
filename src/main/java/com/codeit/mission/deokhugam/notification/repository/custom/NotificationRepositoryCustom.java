package com.codeit.mission.deokhugam.notification.repository.custom;

import com.codeit.mission.deokhugam.notification.dto.NotificationRequestQuery;
import com.codeit.mission.deokhugam.notification.entity.Notification;
import java.util.UUID;
import org.springframework.data.domain.Slice;

public interface NotificationRepositoryCustom {

    Slice<Notification> findByUserWithCursor(
        UUID userId,
        NotificationRequestQuery query
    );

    long countByUserId(UUID userId);
}
