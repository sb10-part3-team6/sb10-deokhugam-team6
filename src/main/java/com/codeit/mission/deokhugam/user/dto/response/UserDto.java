package com.codeit.mission.deokhugam.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "유저 응답")
public record UserDto(
    @Schema(
        description = "사용자 ID",
        example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    )
    UUID id,

    @Schema(
        description = "이메일",
        example = "string@codeit.com"
    )
    String email,

    @Schema(
        description = "닉네임",
        example = "string"
    )
    String nickname,

    @Schema(
        description = "생성 시점",
        example = "2026-04-24T05:19:10.913Z"
    )
    Instant createdAt
) {

}
