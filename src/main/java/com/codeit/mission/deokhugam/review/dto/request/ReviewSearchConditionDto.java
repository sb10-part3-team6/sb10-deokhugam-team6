package com.codeit.mission.deokhugam.review.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

/*
    리뷰 목록 조회 생성 요청 DTO
 */
public record ReviewSearchConditionDto(
        UUID userId,                // 작성자 ID
        UUID bookId,                // 도서 ID
        String keyword,             // 검색 키워드 (작성자 닉네임 | 내용)
        String orderBy,             // 정렬 기준 (createdAt | rating)
        String direction,           // 정렬 방향 (ASC, DESC)
        String cursor,              // 커서
        LocalDateTime after,        // 보조 커서 (createdAt)
        Integer limit               // 페이지 크기
) {
    // 기본값 설정을 위한 생성자
    public ReviewSearchConditionDto {
        if (orderBy == null) {
            orderBy = "created_at";
        }
        if (direction == null) {
            direction = "desc";
        }
        if (limit == null || limit <= 0) {
            limit = 50;
        }
    }
}
