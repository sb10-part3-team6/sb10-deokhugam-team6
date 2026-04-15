
package com.codeit.mission.deokhugam.domain.user.mapper;

import com.codeit.mission.deokhugam.domain.user.dto.UserDto;
import com.codeit.mission.deokhugam.domain.user.dto.UserRegisterRequest;
import com.codeit.mission.deokhugam.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    User toEntity(UserRegisterRequest request);

    UserDto toDto(User user);
}
