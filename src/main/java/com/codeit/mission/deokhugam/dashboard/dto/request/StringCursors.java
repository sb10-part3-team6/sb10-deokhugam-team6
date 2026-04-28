package com.codeit.mission.deokhugam.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가공되지 않은 페이지네이션 커서 정보")
public record StringCursors(
    @Schema(
        description = "커서 페이지네이션 커서",
        example = "string"
    )
    String cursor,

    @Schema(
        description = "보조 커서 (createdAt)",
        example = "2025-04-06T15:04:05.000Z"
    )
    String after
) {

}
