package com.codeit.mission.deokhugam.review.service;

import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.book.repository.BookRepository;
import com.codeit.mission.deokhugam.review.dto.request.ReviewCreateRequest;
import com.codeit.mission.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.codeit.mission.deokhugam.review.dto.response.ReviewDto;
import com.codeit.mission.deokhugam.review.dto.response.ReviewLikeDto;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.entity.ReviewStatus;
import com.codeit.mission.deokhugam.review.exception.DuplicateReviewException;
import com.codeit.mission.deokhugam.review.exception.ReviewAuthorMismatchException;
import com.codeit.mission.deokhugam.review.exception.ReviewNotFoundException;
import com.codeit.mission.deokhugam.review.mapper.ReviewMapper;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplementTest {
    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImplement reviewServiceImplement;

    /*
        리뷰 상세 조회
     */

    // [성공]
    @Test
    @DisplayName("리뷰 상세 조회 완료")
    void find_review_by_id_success() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();

        // 가짜 객체 | 상세 조회 요청자
        User requestUser = User.builder().build();
        ReflectionTestUtils.setField(requestUser, "id", requestUserId);                                    // NPE 방지를 위한 id 강제 주입

        // 조회할 리뷰
        Review savedReview = Review.builder()
                .content("meow meow")
                .rating(5)
                .build();
        ReflectionTestUtils.setField(savedReview, "id", reviewId);                                         // NPE 방지를 위한 id 강제 주입
        ReflectionTestUtils.setField(savedReview, "status", ReviewStatus.ACTIVE);                          // status 강제 주입

        // 내부 로직 흐름 설정
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));                         // savedReview 반환
        given(userRepository.findById(requestUserId)).willReturn(Optional.of(requestUser));                      // requestUser 반환
        given(reviewRepository.existsLikedByIdAndUserId(reviewId, requestUserId)).willReturn(true);        // 특정 리뷰에 대한 사용자의 좋아요 여부

        // 응답 DTO 객체
        ReviewDto expectedReviewDto = ReviewDto.builder()
                .content(savedReview.getContent())
                .rating(savedReview.getRating())
                .likedByMe(true)
                .build();
        given(reviewMapper.toDto(any(Review.class), anyBoolean())).willReturn(expectedReviewDto);                // exceptedReviewDto 반환

        // when
        ReviewDto result = reviewServiceImplement.findById(reviewId, requestUserId);

        // then
        assertNotNull(result);                                                          // 상세 조회 성공 여부
        assertEquals(expectedReviewDto.content(), result.content());                    // 가짜 DTO 객체와 실제 조회 결과 비교
        assertTrue(result.likedByMe());                                                 // 좋아요 여부 반영 확인
    }

    // [실패] 특정 리뷰가 존재하지 않음
    @Test
    @DisplayName("리뷰 상세 조회 실패: 해당 리뷰가 존재하지 않을 경우, REVIEW_NOT_FOUND 예외 반환")
    void find_review_by_id_failure() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();

        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());        // 빈 객체 반환

        // when & then
        assertThrows(ReviewNotFoundException.class, () -> {
            // validateOwner 예외 반환 확인
            reviewServiceImplement.findById(reviewId, requestUserId);
        });
        verify(reviewRepository, never()).existsLikedByIdAndUserId(any(), any());             // Repository의 유효성 검증 (중복 검사) 미호출 확인
        verify(reviewMapper, never()).toDto(any(), anyBoolean());                             // Mapper의 toDto 미호출 확인
    }

    /*
        리뷰 등록
     */

    // [성공]
    @Test
    @DisplayName("리뷰 생성 완료")
    void create_review_success() {
        // given
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // 생성할 리뷰 내용
        ReviewCreateRequest createRequest = new ReviewCreateRequest(
                bookId,
                userId,
                "고양이가 의젓하게 상점 운영도 하고 정말 귀엽네요",
                4
        );

        // 가짜 객체 | 도서 및 사용자
        Book mockBook = Book.builder().build();
        ReflectionTestUtils.setField(mockBook, "id", userId);               // NPE 방지를 위한 id 강제 삽입
        User mockUser = User.builder().build();
        ReflectionTestUtils.setField(mockUser, "id", userId);               // NPE 방지를 위한 id 강제 삽입

        given(reviewRepository.existsByBookIdAndUserId(bookId, userId)).willReturn(false);          // 중복체크 통과
        given(bookRepository.findById(bookId)).willReturn(Optional.of(mockBook));                         // mockBook 반환
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));                         // mockUser 반환

        // 생성할 리뷰
        Review createdReview = Review.builder()
                .content(createRequest.content())
                .rating(createRequest.rating()).
                build();

        // 응답 DTO
        ReviewDto expectedDto = ReviewDto.builder()
                .content(createdReview.getContent())
                .rating(createdReview.getRating())
                .build();

        given(reviewRepository.saveAndFlush(any(Review.class))).willReturn(createdReview);               // createdReview 반환
        given(reviewMapper.toDto(any(Review.class), eq(false))).willReturn(expectedDto);           // exceptedDto 반환

        // when
        ReviewDto result = reviewServiceImplement.create(createRequest);

        // then
        assertNotNull(result);                                                  // 리뷰 등록 여부
        assertEquals(expectedDto.content(), result.content());                  // 가짜 DTO 결과와 실제 실행 결과 비교
        assertEquals(expectedDto.rating(), result.rating());
    }

    // [실패] 특정 리뷰에 대한 사용자의 리뷰 중복 생성 요청
    @Test
    @DisplayName("리뷰 등록 실패: 특정 도서에 이미 사용자의 리뷰가 존재할 경우, DUPLICATE_REVIEW 에러 반환")
    void create_review_failure_duplicate_review() {
        // given
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // 생성할 리뷰 내용
        ReviewCreateRequest createRequest = new ReviewCreateRequest(
                bookId,
                userId,
                "고양이가 의젓하게 상점 운영도 하고 정말 귀엽네요",
                4
        );

        given(reviewRepository.existsByBookIdAndUserId(bookId, userId)).willReturn(true);       // 리뷰 중복

        // when & then
        assertThrows(DuplicateReviewException.class, () -> {
            // validateDuplicateReview 예외 반환 확인
            reviewServiceImplement.create(createRequest);
        });
        verify(bookRepository, never()).findById(any());
        verify(userRepository, never()).findById(any());
        verify(reviewRepository, never()).saveAndFlush(any());
    }

    // [실패] 하나의 리뷰 생성이 완료되기 전, 동일한 사용자로부터 동일한 데이터의 리뷰 생성 요청으로 인한 동시성 문제 발생
    @Test
    @DisplayName("리뷰 등록 실패: 동일한 사용자로부터 똑같은 요청을 연속으로 받아 동시성 이슈가 발생한 경우, DUPLICATE_REVIEW 에러 반환")
    void crate_review_failure_concurrency() {
        // given
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // 생성할 리뷰 내용
        ReviewCreateRequest createRequest = new ReviewCreateRequest(
                bookId,
                userId,
                "고양이가 의젓하게 상점 운영도 하고 정말 귀엽네요",
                4
        );

        // 가짜 객체 | 도서 및 사용자
        Book mockBook = Book.builder().build();
        ReflectionTestUtils.setField(mockBook, "id", userId);               // NPE 방지를 위한 id 강제 삽입
        User mockUser = User.builder().build();
        ReflectionTestUtils.setField(mockUser, "id", userId);               // NPE 방지를 위한 id 강제 삽입

        given(reviewRepository.existsByBookIdAndUserId(bookId, userId)).willReturn(false);          // 중복체크 통과
        given(bookRepository.findById(bookId)).willReturn(Optional.of(mockBook));                         // mockBook 반환
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));                         // mockUser 반환

        // saveAndFlush 시점에 데이터베이스 제약 위반 예외 발생
        DataIntegrityViolationException exception = mock(DataIntegrityViolationException.class);
        Throwable cause = mock(Throwable.class);

        given(exception.getMostSpecificCause()).willReturn(cause);
        given(cause.getMessage()).willReturn("Unique index or primary key violation: uk_book_user");        // 발생한 제약 위반 예외 = 중복 리뷰 예외
        given(reviewRepository.saveAndFlush(any(Review.class))).willThrow(exception);                             // exception 반환

        // when & then
        assertThrows(DuplicateReviewException.class, () -> {
            // try-catch 구문 예외 반환 확인
            reviewServiceImplement.create(createRequest);
        });
    }

    /*
        리뷰 수정
     */

    // [성공]
    @Test
    @DisplayName("리뷰 수정 완료")
    void update_review_success() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // 가짜 객체 | 도서 및 사용자
        Book mockBook = Book.builder().build();
        User mockUser = User.builder().build();
        ReflectionTestUtils.setField(mockUser, "id", userId);                                              // NPE 방지를 위한 id 강제 삽입

        // 기존 리뷰 정보
        Review savedReview = Review.builder()
                .book(mockBook)
                .user(mockUser)
                .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
                .rating(3)
                .build();
        ReflectionTestUtils.setField(savedReview, "id", reviewId);                                         // NPE 방지를 위한 id 강제 주입
        ReflectionTestUtils.setField(savedReview, "status", ReviewStatus.ACTIVE);                          // status 강제 주입

        // 수정할 리뷰 내용
        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(
                "나도 선량한 지구인인데 왜 로키를 만날 수 없는 거지. 질문.",
                5
        );

        // 내부 로직 흐름 설정
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));                        // savedReview 반환
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));                               // mockUser 반환
        given(reviewRepository.existsLikedByIdAndUserId(reviewId, userId)).willReturn(false);             // 특정 리뷰에 대한 사용자의 좋아요 여부

        // 응답 DTO 객체
        ReviewDto expectedDto = ReviewDto.builder()
                .content(updateRequest.content())
                .rating(updateRequest.rating())
                .likedByMe(false)
                .build();
        given(reviewMapper.toDto(any(Review.class), anyBoolean())).willReturn(expectedDto);                     // expectedDto 반환

        // when
        ReviewDto result = reviewServiceImplement.update(reviewId, userId, updateRequest);

        // then
        assertNotNull(result);
        assertEquals(updateRequest.content(), result.content());                    // 가짜 DTO와 실제 실행 결과 확인
        assertEquals(updateRequest.rating(), result.rating());
        verify((reviewMapper)).toDto(savedReview, false);                    // Mapper 호출 내역 확인
    }

    // [실패] 요청자와 리뷰 작성자 불일치
    @Test
    @DisplayName("리뷰 수정 실패: 요청자와 리뷰 작성자가 불일치 할 경우, REVIEW_AUTHOR_MISMATCH 에러 반환")
    void update_review_failure() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();

        // 가짜 객체 | 리뷰 작성자 및 리뷰 수정 요청자
        User author = User.builder().build();
        ReflectionTestUtils.setField(author, "id", userId);                                                 // NPE 방지를 위한 id 강제 삽입
        User requestUser = User.builder().build();
        ReflectionTestUtils.setField(requestUser, "id", requestUserId);

        // 기존 리뷰 정보
        Review savedReview = Review.builder()
                .user(author)
                .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
                .rating(3)
                .build();
        ReflectionTestUtils.setField(savedReview, "id", reviewId);                                         // NPE 방지를 위한 id 강제 주입
        ReflectionTestUtils.setField(savedReview, "status", ReviewStatus.ACTIVE);                          // status 강제 주입

        // 수정할 리뷰 내용
        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(
                "나도 선량한 지구인인데 왜 로키를 만날 수 없는 거지. 질문.",
                5
        );

        // 내부 로직 흐름 설정
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));          // savedReview 반환
        given(userRepository.findById(requestUserId)).willReturn(Optional.of(requestUser));       // requestUser 반환

        // when & then
        assertThrows(ReviewAuthorMismatchException.class, () -> {
            // validateOwner 예외 반환 확인
            reviewServiceImplement.update(reviewId, requestUserId, updateRequest);
        });
        verify(reviewRepository, never()).save(any(Review.class));                      // Repository의 save 함수 미호출 확인
        verify(reviewMapper, never()).toDto(any(Review.class), anyBoolean());           // Mapper의 toDto 함수 미호출 확인
    }

    /*
        리뷰 삭제
     */

    // [논리 삭제 성공]
    @Test
    @DisplayName("리뷰 논리 삭제 성공")
    void delete_review_success() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // 가짜 객체 | 도서 및 사용자
        User mockUser = User.builder().build();
        ReflectionTestUtils.setField(mockUser, "id", userId);                                              // NPE 방지를 위한 id 강제 삽입

        // 삭제할 리뷰 정보
        Review savedReview = Review.builder()
                .user(mockUser)
                .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
                .rating(3)
                .build();
        ReflectionTestUtils.setField(savedReview, "id", reviewId);                                         // NPE 방지를 위한 id 강제 주입
        ReflectionTestUtils.setField(savedReview, "status", ReviewStatus.ACTIVE);                          // status 강제 주입

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));                        // savedReview 반환
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));                               // mockUser 반환

        // when
        reviewServiceImplement.delete(reviewId, userId);

        // then
        assertEquals(ReviewStatus.DELETED, savedReview.getStatus());                // 특정 리뷰의 논리 삭제 여부 검증
        verify(reviewRepository, never()).delete(any(Review.class));                // Repository의 delete 함수 미호출 확인
    }

    // [물리 삭제 성공]
    @Test
    @DisplayName("리뷰 물리 삭제 성공")
    void hard_delete_review_success() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // 가짜 객체 | 도서 및 사용자
        User mockUser = User.builder().build();
        ReflectionTestUtils.setField(mockUser, "id", userId);                                              // NPE 방지를 위한 id 강제 삽입

        // 삭제할 리뷰 정보
        Review savedReview = Review.builder()
                .user(mockUser)
                .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
                .rating(3)
                .build();
        ReflectionTestUtils.setField(savedReview, "id", reviewId);                                         // NPE 방지를 위한 id 강제 주입
        ReflectionTestUtils.setField(savedReview, "status", ReviewStatus.ACTIVE);                          // status 강제 주입

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));                         // savedReview 반환
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));                                // mockUser 반환

        // when
        reviewServiceImplement.hardDelete(reviewId, userId);

        // then
        verify(reviewRepository, times(1)).delete(any(Review.class));                // Repository의 delete 함수 호출 확인
    }

    // [실패] 특정 리뷰가 이미 논리적으로 삭제된 경우
    @Test
    @DisplayName("리뷰 논리 삭제 실패: 특정 리뷰가 이미 논리적으로 삭제된 경우, REVIEW_NOT_FOUND 예외 반환")
    void delete_review_failure() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // 가짜 객체 | 도서 및 사용자
        User mockUser = User.builder().build();
        ReflectionTestUtils.setField(mockUser, "id", userId);                                              // NPE 방지를 위한 id 강제 삽입

        // 삭제할 리뷰 정보
        Review savedReview = Review.builder()
                .user(mockUser)
                .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
                .rating(3)
                .build();
        ReflectionTestUtils.setField(savedReview, "id", reviewId);                                         // NPE 방지를 위한 id 강제 주입
        ReflectionTestUtils.setField(savedReview, "status", ReviewStatus.DELETED);                         // status 강제 주입

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));                         // savedReview 반환
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));                                // mockUser 반환

        // when & then
        assertThrows(ReviewNotFoundException.class, () -> {
            // validateReviewActive 예외 반환 확인
            reviewServiceImplement.delete(reviewId, userId);
        });
        verify(reviewRepository, never()).delete(any());                // Repository 내 delete 미호출 확인
    }

    /*
        리뷰 좋아요 추가 및 취소
     */

    // [성공]
    @Test
    @DisplayName("리뷰 좋아요 추가 성공")
    void add_review_like_success() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // 가짜 객체 | 도서 및 사용자
        User mockUser = User.builder().build();
        ReflectionTestUtils.setField(mockUser, "id", userId);                                              // NPE 방지를 위한 id 강제 삽입

        // 좋아요를 추가할 리뷰 정보
        Review savedReview = Review.builder()
                .user(mockUser)
                .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
                .rating(3)
                .build();
        ReflectionTestUtils.setField(savedReview, "id", reviewId);                                         // NPE 방지를 위한 id 강제 주입
        ReflectionTestUtils.setField(savedReview, "status", ReviewStatus.ACTIVE);                          // status 강제 주입

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));                         // savedReview 반환
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));                                // mockUser 반환
        given(reviewRepository.existsLikedByIdAndUserId(reviewId, userId)).willReturn(false);              // 특정 리뷰에 대한 요청자의 리뷰가 존재하지 않음

        // 응답 DTO
        ReviewLikeDto.builder()
                .reviewId(savedReview.getId())
                .userId(mockUser.getId())
                .liked(true)
                .build();

        // when
        ReviewLikeDto result = reviewServiceImplement.toggleLike(reviewId, userId);

        // then
        assertNotNull(result);
        assertTrue(result.liked());                                          // 실행 결과 확인
        assertEquals(1, savedReview.getLikeCount());                // 특정 리뷰에 추가된 좋아요 개수 확인
        assertTrue(savedReview.getLikedUsers().contains(mockUser));          // 좋아요를 누른 사용자 리스트 내 좋아요 요청자 포함 여부
    }

    // [성공]
    @Test
    @DisplayName("리뷰 좋아요 취소 성공")
    void remove_review_like_success() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // 가짜 객체 | 도서 및 사용자
        User mockUser = User.builder().build();
        ReflectionTestUtils.setField(mockUser, "id", userId);                                              // NPE 방지를 위한 id 강제 삽입

        // 좋아요를 추가할 리뷰 정보
        Review savedReview = Review.builder()
                .user(mockUser)
                .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
                .rating(3)
                .build();
        ReflectionTestUtils.setField(savedReview, "id", reviewId);                                         // NPE 방지를 위한 id 강제 주입
        ReflectionTestUtils.setField(savedReview, "likeCount", 1);                                   // likeCount 강제 주입
        ReflectionTestUtils.setField(savedReview, "status", ReviewStatus.ACTIVE);                          // status 강제 주입

        // 특정 리뷰에 좋아요를 누른 사용자 목록
        List<User> likedUsers = new ArrayList<>();
        likedUsers.add(mockUser);
        ReflectionTestUtils.setField(savedReview, "likedUsers", likedUsers);

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));                         // savedReview 반환
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));                                // mockUser 반환
        given(reviewRepository.existsLikedByIdAndUserId(reviewId, userId)).willReturn(true);               // 특정 리뷰에 대한 요청자의 리뷰가 존재하지 않음

        // 응답 DTO
        ReviewLikeDto.builder()
                .reviewId(savedReview.getId())
                .userId(mockUser.getId())
                .liked(true)
                .build();

        // when
        ReviewLikeDto result = reviewServiceImplement.toggleLike(reviewId, userId);

        // then
        assertNotNull(result);
        assertFalse(result.liked());                                         // 실행 결과 확인
        assertEquals(0, savedReview.getLikeCount());                // 특정 리뷰에 추가된 좋아요 개수 확인
        assertFalse(savedReview.getLikedUsers().contains(mockUser));         // 좋아요를 누른 사용자 리스트 내 좋아요 요청자 포함 여부
    }
}