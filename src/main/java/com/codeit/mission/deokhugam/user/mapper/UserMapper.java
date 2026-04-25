package com.codeit.mission.deokhugam.user.mapper;

import com.codeit.mission.deokhugam.user.dto.request.UserRegisterRequest;
import com.codeit.mission.deokhugam.user.dto.response.UserDto;
import com.codeit.mission.deokhugam.user.entity.User;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UserMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "status", ignore = true)
  User toEntity(UserRegisterRequest request);

  UserDto toDto(User user);
}
