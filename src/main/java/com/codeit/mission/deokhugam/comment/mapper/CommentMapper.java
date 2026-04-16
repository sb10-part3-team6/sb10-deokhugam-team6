package com.codeit.mission.deokhugam.comment.mapper;

import com.codeit.mission.deokhugam.comment.dto.response.CommentDto;
import com.codeit.mission.deokhugam.comment.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "userNickName", source = "userNickName")
    CommentDto toDto(Comment comment, String userNickName);
}
