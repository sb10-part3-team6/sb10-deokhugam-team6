package com.codeit.mission.deokhugam.review.service;

import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.error.ErrorCode;
import com.codeit.mission.deokhugam.review.dto.request.ReviewCreateRequest;
import com.codeit.mission.deokhugam.review.dto.response.ReviewDto;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.exception.DuplicateReviewException;
import com.codeit.mission.deokhugam.review.mapper.ReviewMapper;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
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
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    private final ReviewMapper reviewMapper;

    // 리뷰 등록
    @Override
    @Transactional
    public ReviewDto create(ReviewCreateRequest reviewCreateRequest) {
        // 1. 중복 검사
        validateDuplicateReview(reviewCreateRequest.bookId(), reviewCreateRequest.userId());

        // 2. Book / User 엔티티 조회
        Book book = getBookEntityOrThrow(reviewCreateRequest.bookId());
        User user = getUserEntityOrThrow(reviewCreateRequest.userId());

        // 3. 리뷰 생성
        Review newReview = Review.builder()
                .book(book)
                .user(user)
                .content(reviewCreateRequest.content())
                .rating(reviewCreateRequest.rating())
                .build();
        reviewRepository.save(newReview);

        // 4. 리뷰 응답 DTO 변환 및 반환
        boolean isLiked = likeRepository.existByReviewIdAndUserId(newReview.getId(), newReview.getUser().getId());
        return reviewMapper.toDto(newReview, isLiked);
    }

    // Book 엔티티 반환
    private Book getBookEntityOrThrow(UUID bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow();
    }

    // User 엔티티 반환
    private User getUserEntityOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow();
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
