package com.codeit.mission.deokhugam.comment.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentFindAllRequest(
        @NotNull
        UUID reviewId,

        @Pattern(regexp = "^(ASC|DESC)$", message = "direction은 ASC 또는 DESC만 가능합니다.")
        String direction,
        String cursor,
        LocalDateTime after,

        @Min(value = 1, message = "값은 최소 1 이상이어야 합니다.")
        @Max(value = 100, message = "값은 최대 100 이하여야 합니다.")
        Integer limit
) {
        public CommentFindAllRequest {
                if (limit == null) {
                        limit = 50;
                }

                if (direction == null) {
                        direction = "DESC";
                }
        }
}
