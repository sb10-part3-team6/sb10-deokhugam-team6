package com.codeit.mission.deokhugam.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 정보")
public record UserRegisterRequest(
    @Schema(
        description = "이메일",
        example = "string@codeit.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    String email,

    @Schema(
        description = "닉네임",
        example = "string",
        minLength = 2,
        maxLength = 20,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    String nickname,

    @Schema(
        description = "비밀번호",
        example = "a77DOP0Z7q7Oa%#$t",
        pattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
        message = "비밀번호는 8~20자의 영문 대소문자, 숫자, 특수문자를 포함해야 합니다."
    )
    String password
) {

}
