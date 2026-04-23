package com.codeit.mission.deokhugam.comment.exception;

import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class CommentNotFoundException extends CommentException {
    public CommentNotFoundException(UUID commentId) {
        super(ErrorCode.COMMENT_NOT_FOUND, Map.of("commentId", commentId));
    }
}
