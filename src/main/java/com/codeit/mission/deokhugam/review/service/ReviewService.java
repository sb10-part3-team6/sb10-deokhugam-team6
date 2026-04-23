package com.codeit.mission.deokhugam.review.service;

import com.codeit.mission.deokhugam.review.dto.request.ReviewCreateRequest;
import com.codeit.mission.deokhugam.review.dto.request.ReviewSearchConditionDto;
import com.codeit.mission.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.codeit.mission.deokhugam.review.dto.response.CursorPageResponseReviewDto;
import com.codeit.mission.deokhugam.review.dto.response.ReviewDto;
import com.codeit.mission.deokhugam.review.dto.response.ReviewLikeDto;

import java.util.UUID;

public interface ReviewService {
    // 리뷰 상세 정보 조회
    ReviewDto findById(UUID id, UUID requestUserId);

    // 리뷰 목록 조회
    CursorPageResponseReviewDto<ReviewDto> findAllByKeyword(UUID requestUserId, ReviewSearchConditionDto searchConditionDto);

    // 리뷰 등록
    ReviewDto create(ReviewCreateRequest reviewCreateRequest);

    // 리뷰 수정
    ReviewDto update(UUID id, UUID requestUserId, ReviewUpdateRequest reviewUpdateRequest);

    // 리뷰 논리 삭제
    void delete(UUID id, UUID requestUserId);

    // 리뷰 물리 삭제
    void hardDelete(UUID id, UUID requestUserId);

    // 리뷰 좋아요 추가 및 취소
    ReviewLikeDto toggleLike(UUID id, UUID requestUserId);
}
