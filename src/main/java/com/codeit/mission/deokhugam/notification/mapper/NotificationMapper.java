package com.codeit.mission.deokhugam.notification.mapper;

import com.codeit.mission.deokhugam.notification.dto.response.NotificationDto;
import com.codeit.mission.deokhugam.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

  @Mapping(target = "userId", expression = "java(notification.getUser().getId())")
  @Mapping(target = "reviewId", expression = "java(notification.getReview().getId())")
  NotificationDto toDto(Notification notification);

}
