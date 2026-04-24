package com.codeit.mission.deokhugam.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "수정할 사용자 정보")
public record UserUpdateRequest(
    @Schema(
        description = "닉네임",
        example = "string",
        minLength = 2,
        maxLength = 20,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    String nickname
) {

}
