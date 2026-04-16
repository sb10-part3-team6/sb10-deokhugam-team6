package com.codeit.mission.deokhugam.review.dto.response;

import java.util.UUID;

public record ReviewLikeDto (
        UUID reviewId,          // 리뷰 id
        UUID userId,            // 요청자 id
        Boolean liked           // 요쳥자의 좋아요 여부
) {
}
