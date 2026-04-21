package com.codeit.mission.deokhugam.review.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/*
    리뷰 페이지네이션 응답 DTO
 */
@Builder
public record CursorPageResponseReviewDto<T> (
        List<ReviewDto> content,                // 실제 데이터 목록
        String nextCursor,                      // 다음 페이지 커서
        LocalDateTime nextAfter,                // 이전 페이지의 마지막 요소 생성 시간
        int size,                               // 페이지 크기
        long totalElements,                     // 총 요소 수
        boolean hasNext                         // 다음 페이지 여부
){
}
