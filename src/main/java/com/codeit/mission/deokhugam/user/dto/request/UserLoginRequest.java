package com.codeit.mission.deokhugam.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 정보")
public record UserLoginRequest(
    @Schema(
        description = "이메일",
        example = "string@codeit.com",
        requiredMode = Schema.RequiredMode.REQUIRED

    )
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    String email,

    @Schema(
        description = "비밀번호",
        example = "a77DOP0Z7q7Oa%#$t",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    String password
) {

}
