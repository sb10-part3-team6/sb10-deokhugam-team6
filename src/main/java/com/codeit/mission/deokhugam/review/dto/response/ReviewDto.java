package com.codeit.mission.deokhugam.review.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReviewDto (
        UUID id,                            // 리뷰 id

        UUID bookId,                        // 대상 도서 id
        String bookTitle,                   // 대상 도셔명
        String bookThumbnailUrl,            // 대상 도서 URL 주소

        UUID userId,                        // 작성자 id
        String userNickName,                // 작성자 이름

        String content,                     // 내용
        int rating,                         // 평점
        int likeCount,                      // 좋아요 수
        int commentCount,                   // 댓글 수
        Boolean likedByMe,                  // 작성자의 좋아요 여부

        LocalDateTime createdAt,            // 생성 시점
        LocalDateTime updatedAt             // 수정 시점
) {
}
