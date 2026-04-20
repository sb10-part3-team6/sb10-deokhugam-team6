package com.codeit.mission.deokhugam.book.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;

import static com.codeit.mission.deokhugam.error.ErrorCode.S3_UPLOAD_FAILED;

public class S3UploadFailureException extends DeokhugamException {
    public S3UploadFailureException() {
        super(S3_UPLOAD_FAILED);
    }
}
