package com.codeit.mission.deokhugam.comment.exception;

import com.codeit.mission.deokhugam.error.ErrorCode;

public class CommentAuthorException extends CommentException {
    public CommentAuthorException() {
        super(ErrorCode.FORBIDDEN_COMMENT_UPDATE);
    }

}
