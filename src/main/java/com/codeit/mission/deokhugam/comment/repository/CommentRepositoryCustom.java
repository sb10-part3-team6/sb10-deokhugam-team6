package com.codeit.mission.deokhugam.comment.repository;

import com.codeit.mission.deokhugam.comment.dto.request.CommentFindAllRequest;
import com.codeit.mission.deokhugam.comment.entity.Comment;

import java.util.List;

public interface CommentRepositoryCustom {
    List<Comment> filter(CommentFindAllRequest request);
}
