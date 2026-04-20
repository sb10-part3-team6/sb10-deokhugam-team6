package com.codeit.mission.deokhugam.review.service;

import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.book.repository.BookRepository;
import com.codeit.mission.deokhugam.error.ErrorCode;
import com.codeit.mission.deokhugam.review.dto.request.ReviewCreateRequest;
import com.codeit.mission.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.codeit.mission.deokhugam.review.dto.response.ReviewDto;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.exception.DuplicateReviewException;
import com.codeit.mission.deokhugam.review.exception.ReviewAuthorMismatchException;
import com.codeit.mission.deokhugam.review.exception.ReviewNotFoundException;
import com.codeit.mission.deokhugam.review.mapper.ReviewMapper;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.exception.UserNotFoundException;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/*
    리뷰 서비스
    ----------
    도서 (Book)과 관련된 코드 주석 처리
    관련 내용 병합 이후, 제거 및 테스트 예정
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImplement implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    private final ReviewMapper reviewMapper;

    // 리뷰 상세 조회
    @Override
    public ReviewDto findById(UUID id, UUID requestUserId) {
        // 1. 조정할 리뷰 및 요청자 존재 여부 확인: 존재하지 않을 시, 오류 발생
        Review targetReview = getReviewEntityOrThrow(id);
        User requestUser = getUserEntityOrThrow(requestUserId);

        // 2. 특정 리뷰에 대한 요청자의 좋아요 여부 확인
        boolean isLiked = reviewRepository.existsByIdAndUserId(targetReview.getId(), requestUser.getId());

        // 3. 응답 DTO 변환 및 반환
        return reviewMapper.toDto(targetReview, isLiked);
    }

    // 리뷰 등록
    @Override
    @Transactional
    public ReviewDto create(ReviewCreateRequest reviewCreateRequest) {
        // 1. 중복 검사
        validateDuplicateReview(reviewCreateRequest.bookId(), reviewCreateRequest.userId());

        // 2. 같은 사용자로부터 두 번 이상 생성 요청이 들어온 경우, 동시성 문제 발생 가능
        try {
            // 3. Book / User 엔티티 조회
            //Book book = getBookEntityOrThrow(reviewCreateRequest.bookId());
            User user = getUserEntityOrThrow(reviewCreateRequest.userId());

            // 4. 리뷰 생성
            Review newReview = Review.builder()
                    //.book(book)
                    .user(user)
                    .content(reviewCreateRequest.content())
                    .rating(reviewCreateRequest.rating())
                    .build();
            reviewRepository.save(newReview);

            // 5. 리뷰 응답 DTO 변환 및 반환
            return reviewMapper.toDto(newReview, false);            // 갓 생성한 리뷰는 좋아요 0
          // 만약 동시에 똑같은 요청이 들어와서, DB 유니크 제약 (uk_book_user)가 발생한다면 커스텀 중복 예외 발생
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateReviewException(reviewCreateRequest.bookId(), reviewCreateRequest.userId());
        }
    }

    // 리뷰 수정
    @Override
    @Transactional
    public ReviewDto update(UUID id, UUID requestUserId, ReviewUpdateRequest reviewUpdateRequest) {
        // 1. 수정할 리뷰 및 요청자 존재 여부 확인: 존재하지 않을 시, 오류 발생
        Review targetReview = getReviewEntityOrThrow(id);
        User requestUser = getUserEntityOrThrow(requestUserId);

        // 2. 권한 확인: 본인이 작성한 리뷰에 대해서만 수정 가능
        validateOwner(targetReview, requestUser);

        // 3. 리뷰 수정
        targetReview.updateContentAndRating(reviewUpdateRequest.content(), reviewUpdateRequest.rating());

        // 4. 특정 리뷰에 대한 작성자의 좋아요 여부 확인
        boolean isLiked = reviewRepository.existsByIdAndUserId(targetReview.getId(), requestUser.getId());

        // 5. 리뷰 응답 DTO 반환 및 변환
        return reviewMapper.toDto(targetReview, isLiked);
    }

    // Review 엔티티 반환
    private Review getReviewEntityOrThrow(UUID id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException(id));
    }

    // Book 엔티티 반환
    private Book getBookEntityOrThrow(UUID bookId) {
        return bookRepository.findById(bookId)
                // 헤딩 커스텀 예외 클래스 구현 시, 적용 예정
                .orElseThrow();
    }

    // User 엔티티 반환
    private User getUserEntityOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    // 유효성 검증 (중복 검사): 사용자가 이미 특정 도서에 리뷰를 남긴 경우, 예외 발생
    private void validateDuplicateReview(UUID bookId, UUID userId) {
        if (reviewRepository.existsByBookIdAndUserId(bookId, userId)) {
            throw new DuplicateReviewException(bookId, userId);
        }
    }

    // 유효성 검증 (권한 확인): 요청자와 리뷰 작성자가 다를 경우, 예외 발생
    private void validateOwner (Review targetReview, User requestUser) {
        boolean isOwner = targetReview.getUser().getId().equals(requestUser.getId());

        if (!isOwner) {
            throw  new ReviewAuthorMismatchException(targetReview.getUser().getId(), requestUser.getId());
        }
    }
}
