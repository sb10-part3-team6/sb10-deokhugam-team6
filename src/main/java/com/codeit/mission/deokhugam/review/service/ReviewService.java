package com.codeit.mission.deokhugam.review.service;

import com.codeit.mission.deokhugam.review.dto.request.ReviewCreateRequest;
import com.codeit.mission.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.codeit.mission.deokhugam.review.dto.response.ReviewDto;

import java.util.UUID;

public interface ReviewService {
    // 리뷰 상세 정보 조회
    // 리뷰 목록 조회

    // 리뷰 등록
    ReviewDto create(ReviewCreateRequest reviewCreateRequest);

    // 리뷰 수정
    ReviewDto update(UUID id, UUID requestUserId, ReviewUpdateRequest reviewUpdateRequest);

    // 리뷰 논리 삭제
    // 리뷰 물리 삭제
    // 리뷰 좋아요
}
