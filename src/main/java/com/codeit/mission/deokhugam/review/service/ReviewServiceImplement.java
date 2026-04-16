package com.codeit.mission.deokhugam.review.service;

import com.codeit.mission.deokhugam.error.ErrorCode;
import com.codeit.mission.deokhugam.review.dto.request.ReviewCreateRequest;
import com.codeit.mission.deokhugam.review.dto.response.ReviewDto;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.exception.DuplicateReviewException;
import com.codeit.mission.deokhugam.review.mapper.ReviewMapper;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/*
    리뷰 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImplement implements ReviewService {
    private final ReviewRepository reviewRepository;

    private final ReviewMapper reviewMapper;

    // 리뷰 등록
    @Override
    @Transactional
    public ReviewDto create(ReviewCreateRequest reviewCreateRequest) {
        // 1. 중복 검사
        validateDuplicateReview(reviewCreateRequest.bookId(), reviewCreateRequest.userId());

        // 2. 리뷰 생성
        Review newReview = Review.builder()
                .bookId(reviewCreateRequest.bookId())
                .userId(reviewCreateRequest.userId())
                .content(reviewCreateRequest.content())
                .rating(reviewCreateRequest.rating())
                .build();
        reviewRepository.save(newReview);

        // 3. 리뷰 응답 DTO 변환 및 반환
        return reviewMapper.toDto(newReview);
    }

    // 유효성 검증 (중복 검사): 사용자가 이미 특정 도서에 리뷰를 남긴 경우, 예외 발생
    private void validateDuplicateReview(UUID bookId, UUID userId) {
        if (reviewRepository.existsByBookIdAndUserId(bookId, userId)) {
            throw new DuplicateReviewException(
                    ErrorCode.DUPLICATE_REVIEWS,
                    Map.of(
                            "bookId", bookId,
                            "userId", userId
                    )
            );
        }
    }
}
