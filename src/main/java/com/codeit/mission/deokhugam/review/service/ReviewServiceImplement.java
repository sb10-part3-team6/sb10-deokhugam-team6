package com.codeit.mission.deokhugam.review.service;

import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.book.exception.BookNotFoundException;
import com.codeit.mission.deokhugam.book.repository.BookRepository;
import com.codeit.mission.deokhugam.review.dto.request.ReviewCreateRequest;
import com.codeit.mission.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.codeit.mission.deokhugam.review.dto.response.ReviewDto;
import com.codeit.mission.deokhugam.review.dto.response.ReviewLikeDto;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.entity.ReviewStatus;
import com.codeit.mission.deokhugam.review.exception.*;
import com.codeit.mission.deokhugam.review.mapper.ReviewMapper;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.exception.UserNotFoundException;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final ReviewMapper reviewMapper;

    // 리뷰 상세 조회
    @Override
    public ReviewDto findById(UUID id, UUID requestUserId) {
        // 1. 조정할 리뷰 및 요청자 존재 여부 확인: 존재하지 않을 시, 오류 발생
        Review targetReview = getReviewEntityOrThrow(id);
        User requestUser = getUserEntityOrThrow(requestUserId);

        // 2. 해당 리뷰가 이미 논리적으로 삭제되어 있는지 확인: 이미 논리적으로 삭제된 경우, 오류 발생
        validateReviewActive(targetReview);

        // 3. 특정 리뷰에 대한 요청자의 좋아요 여부 확인
        boolean isLiked = isReviewLiked(targetReview.getId(), requestUser.getId());

        // 4. 응답 DTO 변환 및 반환
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
            Book book = getBookEntityOrThrow(reviewCreateRequest.bookId());
            User user = getUserEntityOrThrow(reviewCreateRequest.userId());

            // 4. 리뷰 생성
            Review newReview = Review.builder()
                    .book(book)
                    .user(user)
                    .content(reviewCreateRequest.content())
                    .rating(reviewCreateRequest.rating())
                    .build();

            // 5. 리뷰 저장 및 즉시 반영하여, try-catch 블록 내에서 제약 조건 위반 예외 포착
            reviewRepository.saveAndFlush(newReview);

            // 6. 리뷰 응답 DTO 변환 및 반환
            return reviewMapper.toDto(newReview, false);            // 갓 생성한 리뷰는 좋아요 0

          // 만약 동시에 똑같은 요청이 들어와서, DB 유니크 제약 (uk_book_user)가 발생한다면 커스텀 중복 예외 발생
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 인한 중복 데이터 삽입 시 발생하는 특정 제약 조건 위반인지 확인
            if (!isDuplicateReviewConstraintViolation(e)){
                // 중복 리뷰가 아닌 다른 무결성 제약 위반 에러
                throw e;
            }
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

        // 2. 해당 리뷰가 이미 논리적으로 삭제되어 있는지 확인: 이미 논리적으로 삭제된 경우, 오류 발생
        validateReviewActive(targetReview);

        // 3. 권한 확인: 본인이 작성한 리뷰에 대해서만 수정 가능
        validateOwner(targetReview, requestUser);

        // 4. 리뷰 수정
        targetReview.updateContentAndRating(reviewUpdateRequest.content(), reviewUpdateRequest.rating());

        // 5. 특정 리뷰에 대한 작성자의 좋아요 여부 확인
        boolean isLiked = isReviewLiked(targetReview.getId(), requestUser.getId());

        // 6. 리뷰 응답 DTO 반환 및 변환
        return reviewMapper.toDto(targetReview, isLiked);
    }

    // 리뷰 논리 삭제
    @Override
    @Transactional
    public void delete(UUID id, UUID requestUserId) {
        // 1. 삭제할 리뷰 및 요청자 존재 여부 확인: 존재하지 않을 시, 오류 발생
        Review targetReview = getReviewEntityOrThrow(id);
        User requestUser = getUserEntityOrThrow(requestUserId);

        // 2. 해당 리뷰가 이미 논리적으로 삭제되어 있는지 확인: 이미 논리적으로 삭제된 경우, 오류 발생
        validateReviewActive(targetReview);

        // 3. 권한 확인: 본인이 작성한 리뷰에 대해서만 삭제 가능
        validateOwner(targetReview, requestUser);

        // 4. 리뷰 논리 삭제
        targetReview.delete();
    }

    // 리뷰 물리 삭제
    @Override
    @Transactional
    public void hardDelete(UUID id, UUID requestUserId) {
        // 1. 삭제할 리뷰 및 요청자 존재 여부 확인: 존재하지 않을 시, 오류 발생
        Review targetReview = getReviewEntityOrThrow(id);
        User requestUser = getUserEntityOrThrow(requestUserId);

        // 2. 권한 확인: 본인이 작성한 리뷰에 대해서만 삭제 가능
        validateOwner(targetReview, requestUser);

        // 3. 리뷰 물리 삭제
        reviewRepository.delete(targetReview);
    }

    // 리뷰 좋아요 추가 및 취소
    @Override
    @Transactional
    public ReviewLikeDto toggleLike(UUID id, UUID requestUserId) {
        // 1. 좋아요를 생성할 리뷰 및 요청자 존재 여부 확인: 존재하지 않을 시, 오류 발생
        Review targetReview = getReviewEntityOrThrow(id);
        User requestUser = getUserEntityOrThrow(requestUserId);


        // 2. 해당 리뷰가 이미 논리적으로 삭제되어 있는지 확인: 이미 논리적으로 삭제된 경우, 오류 발생
        validateReviewActive(targetReview);

        // 3. 좋아요 생성 및 취소
        boolean isLiked = executeToggleWithConcurrencyHandle(targetReview, requestUser);

        // 4. 응답 DTO 생성 및 반환
        return ReviewLikeDto.builder()
                .reviewId(targetReview.getId())
                .userId(requestUser.getId())
                .liked(isLiked)
                .build();
    }

    // Review 엔티티 반환
    private Review getReviewEntityOrThrow(UUID id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException(id));
    }

    // Book 엔티티 반환
    private Book getBookEntityOrThrow(UUID bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new);
    }

    // User 엔티티 반환
    private User getUserEntityOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    // 좋아요 추가 및 생성 동시성 문제 해결을 위한 메서드
    private boolean executeToggleWithConcurrencyHandle(Review review, User requestUser) {
        try {
            // 특정 리뷰에 대한 사용자의 좋아요가 존재하지 않을 경우, 좋아요 추가
            if (!isReviewLiked(review.getId(), requestUser.getId())) {
                processAddLike(review, requestUser);
                return true;
            } else {
                // 특정 리뷰에 대한 사용자의 좋아요가 존재할 경우, 좋아요 취소
                processRemoveLike(review, requestUser);
                return false;
            }
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 인한 중복 데이터 삽입 시 발생하는 특정 제약 조건 위반인지 확인
            if (!isDuplicateReviewLikeConstraintViolation(e)) {
                // 리뷰 좋아요 요청이 아닌 예외
                throw e;
            }
            throw new DuplicateReviewLikeRequestException(review.getId(), requestUser.getId());
        }
    }

    // 좋아요 수 증가
    private void processAddLike(Review review, User user) {
        // 1. 여러 사용자의 요청을 동시에 처리하기 위해 데이터베이스 직접 수정 (likeCount += 1)
        reviewRepository.incrementLikes(review.getId());

        // 2. 특정 도서의 좋아요를 남긴 사용자 목록 업데이트 (추가)
        review.incrementLikesCount(user);

        // 3. 데이터베이스 즉시 반영
        reviewRepository.saveAndFlush(review);
    }

    // 좋아요 수 감소
    private void processRemoveLike(Review review, User user) {
        // 1. 실제 삭제된 특정 리뷰에 대한 사용자의 좋아요 수 반환
        int deletedCount = reviewRepository.deleteReviewLike(review.getId(), user.getId());

        // 실제 삭제된 데이터가 있을 때만, likeCount 감소
        if (deletedCount > 0) {
            // 2. 여러 사용자의 요청을 동시에 처리하기 위해 데이터베이스 직접 수정 (likeCount -= 1)
            reviewRepository.decrementLikes(review.getId());
            // 3. 특정 도서의 좋아요를 남긴 사용자 목록 업데이트 (제거)
            review.decrementLikesCount(user);
        } else {
            // 이미 다른 스레드가 삭제했다면 아무것도 하지 않거나 예외 처리
            throw new ReviewLikeNotFoundException(review.getId(), user.getId());
        }
    }

    // 유효성 검증 (중복 검사): 사용자가 이미 특정 도서에 리뷰를 남긴 경우, 예외 발생
    private void validateDuplicateReview(UUID bookId, UUID userId) {
        if (reviewRepository.existsByBookIdAndUserId(bookId, userId)) {
            throw new DuplicateReviewException(bookId, userId);
        }
    }

    // 유효성 검증 (권한 확인): 요청자와 리뷰 작성자가 다를 경우, 예외 발생
    private void validateOwner(Review targetReview, User requestUser) {
        boolean isOwner = targetReview.getUser().getId().equals(requestUser.getId());

        if (!isOwner) {
            throw  new ReviewAuthorMismatchException(targetReview.getUser().getId(), requestUser.getId());
        }
    }

    // 유효성 검증 (논리 삭제 여부 확인): 이미 논리적으로 삭제된 리뷰일 경우, 예외 발생
    private void validateReviewActive(Review targetReview) {
         if (targetReview.getStatus() == ReviewStatus.DELETED) {
            throw  new ReviewNotFoundException(targetReview.getId());
        }
    }

    // 특정 사용자의 리뷰 좋아요 여부 확인
    private boolean isReviewLiked(UUID reviewId, UUID userId) {
        return reviewRepository.existsLikedByIdAndUserId(reviewId, userId);
    }

    // 유니크 제약 조건 (uk_book_user) 위반 확인: 발생한 예외가 중복 리뷰 예외에 해당하는지 확인
    private boolean isDuplicateReviewConstraintViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getMostSpecificCause();

        return cause != null && cause.getMessage() != null && cause.getMessage().contains("uk_book_user");
    }

    // 유니크 제약 조건 (uk_review_user_like) 위반 확인: 발생한 예외가 중복 리뷰 좋아요 요청 예외에 해당하는지 확인
    private boolean isDuplicateReviewLikeConstraintViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getMostSpecificCause();

        return cause != null && cause.getMessage() != null && cause.getMessage().contains("uk_review_user_like");
    }
}
