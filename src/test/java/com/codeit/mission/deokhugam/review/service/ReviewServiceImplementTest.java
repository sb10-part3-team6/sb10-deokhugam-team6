package com.codeit.mission.deokhugam.review.service;

import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.book.repository.BookRepository;
import com.codeit.mission.deokhugam.review.dto.request.ReviewCreateRequest;
import com.codeit.mission.deokhugam.review.dto.request.ReviewSearchConditionDto;
import com.codeit.mission.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.codeit.mission.deokhugam.review.dto.response.CursorPageResponseReviewDto;
import com.codeit.mission.deokhugam.review.dto.response.ReviewDto;
import com.codeit.mission.deokhugam.review.dto.response.ReviewLikeDto;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.entity.ReviewLike;
import com.codeit.mission.deokhugam.review.entity.ReviewStatus;
import com.codeit.mission.deokhugam.review.exception.DuplicateReviewException;
import com.codeit.mission.deokhugam.review.exception.DuplicateReviewLikeRequestException;
import com.codeit.mission.deokhugam.review.exception.ReviewAuthorMismatchException;
import com.codeit.mission.deokhugam.review.exception.ReviewNotFoundException;
import com.codeit.mission.deokhugam.review.mapper.ReviewMapper;
import com.codeit.mission.deokhugam.review.repository.ReviewLikeRepository;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Collections;
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
  private ReviewLikeRepository reviewLikeRepository;

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
    ReflectionTestUtils.setField(requestUser, "id",
        requestUserId);                                             // NPE 방지를 위한 id 강제 주입

    // 조회할 리뷰
    Review savedReview = Review.builder()
        .content("meow meow")
        .rating(5)
        .build();
    ReflectionTestUtils.setField(savedReview, "id",
        reviewId);                                                  // NPE 방지를 위한 id 강제 주입
    ReflectionTestUtils.setField(savedReview, "status",
        ReviewStatus.ACTIVE);                                       // status 강제 주입

    // 내부 로직 흐름 설정
    given(reviewRepository.findById(reviewId)).willReturn(
        Optional.of(savedReview));                                  // savedReview 반환
    given(userRepository.findById(requestUserId)).willReturn(
        Optional.of(requestUser));                                  // requestUser 반환
    given(reviewLikeRepository.existsByReviewIdAndUserId(reviewId, requestUserId)).willReturn(
        true);                                                // 특정 리뷰에 대한 사용자의 좋아요 여부

    // 응답 DTO 객체
    ReviewDto expectedReviewDto = ReviewDto.builder()
        .content(savedReview.getContent())
        .rating(savedReview.getRating())
        .likedByMe(true)
        .build();
    given(reviewMapper.toDto(any(Review.class), anyBoolean())).willReturn(
        expectedReviewDto);                                         // exceptedReviewDto 반환

    // when
    ReviewDto result = reviewServiceImplement.findById(reviewId, requestUserId);

    // then
    assertNotNull(result);                                                          // 상세 조회 성공 여부
    assertEquals(expectedReviewDto.content(),
        result.content());                                                          // 가짜 DTO 객체와 실제 조회 결과 비교
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
    verify(reviewLikeRepository, never()).existsByReviewIdAndUserId(any(),
        any());                                    // Repository의 유효성 검증 (중복 검사) 미호출 확인
    verify(reviewMapper, never()).toDto(any(),
        anyBoolean());                             // Mapper의 toDto 미호출 확인
  }

  /*
      리뷰 목록 조회
   */
  // [성공] 검색 결과가 존재하는 경우
  @Test
  @DisplayName("정렬 및 페이지네이션이 적용된 리뷰 목록 조회 완료")
  void find_all_by_keyword_success() {
    // given
    UUID requestUserId = UUID.randomUUID();

    // 조회할 리뷰 목록 정보
    ReviewSearchConditionDto condition = new ReviewSearchConditionDto(
        null,
        null,
        null,
        "rating",
        "desc",
        null,
        null,
        2
    );

    // 가짜 객체 | 리뷰
    Review review1 = Review.builder()
        .rating(5)
        .content("에로스")
        .build();
    ReflectionTestUtils.setField(review1, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(review1, "createdAt", LocalDateTime.now().minusDays(1));

    Review review2 = Review.builder()
        .rating(4)
        .content("안테로스")
        .build();
    ReflectionTestUtils.setField(review2, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(review2, "createdAt", LocalDateTime.now().minusDays(2));

    Review review3 = Review.builder()
        .rating(3)
        .content("외계인 고양이")
        .build();
    ReflectionTestUtils.setField(review3, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(review3, "createdAt", LocalDateTime.now().minusDays(3));

    List<Review> mockReviews = List.of(review1, review2, review3);
    List<Review> pagedReviews = List.of(review1, review2);
    List<UUID> reviewIds = List.of(review1.getId(), review2.getId());
    List<UUID> reviewLikeIds = List.of(review1.getId());

    // 가짜 객체 | 응답 DTO
    ReviewDto dto1 = ReviewDto.builder().
        id(review1.getId())
        .content(review1.getContent())
        .likedByMe(true)
        .build();
    ReviewDto dto2 = ReviewDto.builder()
        .id(review2.getId())
        .content(review2.getContent())
        .likedByMe(false)
        .build();
    List<ReviewDto> dtoList = List.of(dto1, dto2);

    // 페이지 응답 DTO
    CursorPageResponseReviewDto<ReviewDto> expectedResponse = CursorPageResponseReviewDto.<ReviewDto>builder()
        .content(dtoList)
        .hasNext(true)
        .build();

    given(reviewRepository.searchReviews(condition)).willReturn(mockReviews);
    given(reviewLikeRepository.findReviewIdsByUserIdAndReviewIdIn(requestUserId,
        reviewIds)).willReturn(
        reviewLikeIds);
    given(reviewMapper.toDtoList(pagedReviews, reviewLikeIds)).willReturn(dtoList);
    given(reviewRepository.countWithFilter(condition)).willReturn(15L);
    given(
        reviewMapper.toCursorPageResponse(eq(dtoList), anyString(), any(LocalDateTime.class), eq(2),
            eq(15L), eq(true)))
        .willReturn(expectedResponse);

    // when
    CursorPageResponseReviewDto<ReviewDto> result = reviewServiceImplement.findAllByKeyword(
        requestUserId, condition);

    // then
    assertNotNull(result);
    assertTrue(result.hasNext());                                                   // 다음 페이지 존재 여부
    assertEquals(2, result.content().size());                              // 페이지된 리뷰 개수 확인
    verify(reviewRepository).searchReviews(condition);
    verify(reviewLikeRepository).findReviewIdsByUserIdAndReviewIdIn(requestUserId, reviewIds);
  }

  // [성공]
  @Test
  @DisplayName("검색 결과가 없는 리뷰 목록 조회 완료")
  void find_all_by_keyword_success_empty() {
    // given
    UUID requestUserId = UUID.randomUUID();

    // 조회할 리뷰 목록 정보
    ReviewSearchConditionDto condition = new ReviewSearchConditionDto(
        null,
        null,
        "Noting",
        "createdAt",
        "desc",
        null,
        null,
        10
    );

    // 빈 결과 리스트 반환 모킹
    given(reviewRepository.searchReviews(condition)).willReturn(Collections.emptyList());
    given(reviewMapper.toDtoList(Collections.emptyList(), Collections.emptyList())).willReturn(
        Collections.emptyList());
    given(reviewRepository.countWithFilter(condition)).willReturn(0L);

    // 페이지 응답 DTO
    CursorPageResponseReviewDto<ReviewDto> expectedResponse = CursorPageResponseReviewDto.<ReviewDto>builder()
        .content(Collections.emptyList())
        .hasNext(false)
        .totalElements(0L)
        .build();

    // 매퍼 호출 매칭 (데이터가 없으니 nextCursor, nextAfter는 null로 들어감)
    given(reviewMapper.toCursorPageResponse(Collections.emptyList(), null, null, 10, 0L, false))
        .willReturn(expectedResponse);

    // when
    CursorPageResponseReviewDto<ReviewDto> result = reviewServiceImplement.findAllByKeyword(
        requestUserId, condition);

    // then
    assertNotNull(result);
    assertFalse(result.hasNext());                                                // 다음 페이지 존재 여부
    assertTrue(
        result.content().isEmpty());                                       // 검색 결과가 빈 내용인지 확인
    assertEquals(0L, result.totalElements());
    verify(reviewRepository).searchReviews(condition);
    verify(reviewLikeRepository, never()).findReviewIdsByUserIdAndReviewIdIn(any(), any());
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
    ReflectionTestUtils.setField(mockBook, "id", bookId);               // NPE 방지를 위한 id 강제 삽입
    User mockUser = User.builder().build();
    ReflectionTestUtils.setField(mockUser, "id", userId);               // NPE 방지를 위한 id 강제 삽입

    given(reviewRepository.existsByBookIdAndUserId(bookId, userId)).willReturn(
        false);                                   // 중복체크 통과
    given(bookRepository.findById(bookId)).willReturn(
        Optional.of(mockBook));                         // mockBook 반환
    given(userRepository.findById(userId)).willReturn(
        Optional.of(mockUser));                         // mockUser 반환

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

    given(reviewRepository.saveAndFlush(any(Review.class))).willReturn(
        createdReview);                                                     // createdReview 반환
    given(reviewMapper.toDto(any(Review.class), eq(false))).willReturn(
        expectedDto);                                                       // exceptedDto 반환

    // when
    ReviewDto result = reviewServiceImplement.create(createRequest);

    // then
    assertNotNull(result);                                                  // 리뷰 등록 여부
    assertEquals(expectedDto.content(),
        result.content());                                                  // 가짜 DTO 결과와 실제 실행 결과 비교
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
    ReflectionTestUtils.setField(mockBook, "id", bookId);               // NPE 방지를 위한 id 강제 삽입
    User mockUser = User.builder().build();
    ReflectionTestUtils.setField(mockUser, "id", userId);               // NPE 방지를 위한 id 강제 삽입

    given(reviewRepository.existsByBookIdAndUserId(bookId, userId)).willReturn(
        false);                                   // 중복체크 통과
    given(bookRepository.findById(bookId)).willReturn(
        Optional.of(mockBook));                         // mockBook 반환
    given(userRepository.findById(userId)).willReturn(
        Optional.of(mockUser));                         // mockUser 반환

    // saveAndFlush 시점에 데이터베이스 제약 위반 예외 발생
    DataIntegrityViolationException exception = mock(DataIntegrityViolationException.class);
    Throwable cause = mock(Throwable.class);

    given(exception.getMostSpecificCause()).willReturn(cause);
    given(cause.getMessage()).willReturn(
        "Unique index or primary key violation: uk_book_user");        // 발생한 제약 위반 예외 = 중복 리뷰 예외
    given(reviewRepository.saveAndFlush(any(Review.class))).willThrow(
        exception);                                                          // exception 반환

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
    ReflectionTestUtils.setField(mockUser, "id",
        userId);                                                           // NPE 방지를 위한 id 강제 삽입

    // 기존 리뷰 정보
    Review savedReview = Review.builder()
        .book(mockBook)
        .user(mockUser)
        .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
        .rating(3)
        .build();
    ReflectionTestUtils.setField(savedReview, "id",
        reviewId);                                                        // NPE 방지를 위한 id 강제 주입
    ReflectionTestUtils.setField(savedReview, "status",
        ReviewStatus.ACTIVE);                                             // status 강제 주입

    // 수정할 리뷰 내용
    ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(
        "나도 선량한 지구인인데 왜 로키를 만날 수 없는 거지. 질문.",
        5
    );

    // 내부 로직 흐름 설정
    given(reviewRepository.findById(reviewId)).willReturn(
        Optional.of(savedReview));                                        // savedReview 반환
    given(userRepository.findById(userId)).willReturn(
        Optional.of(mockUser));                                           // mockUser 반환
    given(reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId)).willReturn(
        false);                                                     // 특정 리뷰에 대한 사용자의 좋아요 여부

    // 응답 DTO 객체
    ReviewDto expectedDto = ReviewDto.builder()
        .content(updateRequest.content())
        .rating(updateRequest.rating())
        .likedByMe(false)
        .build();
    given(reviewMapper.toDto(any(Review.class), anyBoolean())).willReturn(
        expectedDto);                                                    // expectedDto 반환

    // when
    ReviewDto result = reviewServiceImplement.update(reviewId, userId, updateRequest);

    // then
    assertNotNull(result);
    assertEquals(updateRequest.content(),
        result.content());                                                       // 가짜 DTO와 실제 실행 결과 확인
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
    ReflectionTestUtils.setField(author, "id",
        userId);                                                        // NPE 방지를 위한 id 강제 삽입
    User requestUser = User.builder().build();
    ReflectionTestUtils.setField(requestUser, "id", requestUserId);

    // 기존 리뷰 정보
    Review savedReview = Review.builder()
        .user(author)
        .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
        .rating(3)
        .build();
    ReflectionTestUtils.setField(savedReview, "id",
        reviewId);                                                      // NPE 방지를 위한 id 강제 주입
    ReflectionTestUtils.setField(savedReview, "status",
        ReviewStatus.ACTIVE);                                           // status 강제 주입

    // 수정할 리뷰 내용
    ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(
        "나도 선량한 지구인인데 왜 로키를 만날 수 없는 거지. 질문.",
        5
    );

    // 내부 로직 흐름 설정
    given(reviewRepository.findById(reviewId)).willReturn(
        Optional.of(savedReview));                                      // savedReview 반환
    given(userRepository.findById(requestUserId)).willReturn(
        Optional.of(requestUser));                                      // requestUser 반환

    // when & then
    assertThrows(ReviewAuthorMismatchException.class, () -> {
      // validateOwner 예외 반환 확인
      reviewServiceImplement.update(reviewId, requestUserId, updateRequest);
    });
    verify(reviewRepository, never()).save(
        any(Review.class));                                             // Repository의 save 함수 미호출 확인
    verify(reviewMapper, never()).toDto(any(Review.class),
        anyBoolean());                                                  // Mapper의 toDto 함수 미호출 확인
  }

    /*
        리뷰 논리 삭제
     */

  // [성공]
  @Test
  @DisplayName("리뷰 논리 삭제 성공")
  void delete_review_success() {
    // given
    UUID reviewId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    // 가짜 객체 | 논리 삭제 요청자
    User mockUser = User.builder().build();
    ReflectionTestUtils.setField(mockUser, "id",
        userId);                                                      // NPE 방지를 위한 id 강제 삽입

    // 삭제할 리뷰 정보
    Review savedReview = Review.builder()
        .user(mockUser)
        .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
        .rating(3)
        .build();
    ReflectionTestUtils.setField(savedReview, "id",
        reviewId);                                                    // NPE 방지를 위한 id 강제 주입
    ReflectionTestUtils.setField(savedReview, "status",
        ReviewStatus.ACTIVE);                                         // status 강제 주입

    given(reviewRepository.findById(reviewId)).willReturn(
        Optional.of(savedReview));                                    // savedReview 반환
    given(userRepository.findById(userId)).willReturn(
        Optional.of(mockUser));                                       // mockUser 반환

    // when
    reviewServiceImplement.delete(reviewId, userId);

    // then
    assertEquals(ReviewStatus.DELETED,
        savedReview.getStatus());                                     // 특정 리뷰의 논리 삭제 여부 검증
    verify(reviewRepository, never()).delete(
        any(Review.class));                                           // Repository의 delete 함수 미호출 확인
  }

  // [실패] 특정 리뷰가 이미 논리적으로 삭제된 경우
  @Test
  @DisplayName("리뷰 논리 삭제 실패: 특정 리뷰가 이미 논리적으로 삭제된 경우, REVIEW_NOT_FOUND 예외 반환")
  void delete_review_failure() {
    // given
    UUID reviewId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    // 가짜 객체 | 논리 삭제 요청자
    User mockUser = User.builder().build();
    ReflectionTestUtils.setField(mockUser, "id",
        userId);                                                      // NPE 방지를 위한 id 강제 삽입

    // 삭제할 리뷰 정보
    Review savedReview = Review.builder()
        .user(mockUser)
        .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
        .rating(3)
        .build();
    ReflectionTestUtils.setField(savedReview, "id",
        reviewId);                                                    // NPE 방지를 위한 id 강제 주입
    ReflectionTestUtils.setField(savedReview, "status",
        ReviewStatus.DELETED);                                        // status 강제 주입

    given(reviewRepository.findById(reviewId)).willReturn(
        Optional.of(savedReview));                                    // savedReview 반환
    given(userRepository.findById(userId)).willReturn(
        Optional.of(mockUser));                                       // mockUser 반환

    // when & then
    assertThrows(ReviewNotFoundException.class, () -> {
      // validateReviewActive 예외 반환 확인
      reviewServiceImplement.delete(reviewId, userId);
    });
    verify(reviewRepository, never()).delete(any());                 // Repository 내 delete 미호출 확인
  }

    /*
        리뷰 물리 삭제
     */

  // [물리 삭제 성공]
  @Test
  @DisplayName("리뷰 물리 삭제 성공")
  void hard_delete_review_success() {
    // given
    UUID reviewId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    // 가짜 객체 | 물리 삭제 요청자
    User mockUser = User.builder().build();
    ReflectionTestUtils.setField(mockUser, "id",
        userId);                                                // NPE 방지를 위한 id 강제 삽입

    // 삭제할 리뷰 정보
    Review savedReview = Review.builder()
        .user(mockUser)
        .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
        .rating(3)
        .build();
    ReflectionTestUtils.setField(savedReview, "id",
        reviewId);                                              // NPE 방지를 위한 id 강제 주입
    ReflectionTestUtils.setField(savedReview, "status",
        ReviewStatus.ACTIVE);                                   // status 강제 주입

    given(reviewRepository.findById(reviewId)).willReturn(
        Optional.of(savedReview));                              // savedReview 반환
    given(userRepository.findById(userId)).willReturn(
        Optional.of(mockUser));                                 // mockUser 반환

    // when
    reviewServiceImplement.hardDelete(reviewId, userId);

    // then
    verify(reviewRepository, times(1)).delete(
        any(Review.class));                                     // Repository의 delete 함수 호출 확인
  }


  // [실패] 작성자 요청자 불일치
  @Test
  @DisplayName("리뷰 물리 삭제 실패: 요청자와 리뷰 작성자가 불일치 할 경우, REVIEW_AUTHOR_MISMATCH 에러 반환")
  void hard_delete_review_failure() {
    // given
    UUID reviewId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    UUID requestUserId = UUID.randomUUID();

    // 가짜 객체 | 리뷰 작성자 및 논리 삭제 요청자
    User author = User.builder().build();
    ReflectionTestUtils.setField(author, "id",
        authorId);                                                    // NPE 방지를 위한 id 강제 삽입
    User requestUser = User.builder().build();
    ReflectionTestUtils.setField(requestUser, "id", requestUserId);

    // 삭제할 리뷰 정보
    Review savedReview = Review.builder()
        .user(author)
        .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
        .rating(3)
        .build();
    ReflectionTestUtils.setField(savedReview, "id",
        reviewId);                                                    // NPE 방지를 위한 id 강제 주입
    ReflectionTestUtils.setField(savedReview, "status",
        ReviewStatus.ACTIVE);                                         // status 강제 주입

    given(reviewRepository.findById(reviewId)).willReturn(
        Optional.of(savedReview));                                    // savedReview 반환
    given(userRepository.findById(requestUserId)).willReturn(
        Optional.of(requestUser));                                    // requestUser 반환

    // when
    assertThrows(ReviewAuthorMismatchException.class, () -> {
      // validateOwner 예외 반환 확인
      reviewServiceImplement.hardDelete(savedReview.getId(), requestUser.getId());
    });
    verify(reviewRepository, never()).delete(any(Review.class));
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
    ReflectionTestUtils.setField(mockUser, "id",
        userId);                                                  // NPE 방지를 위한 id 강제 삽입

    // 좋아요를 추가할 리뷰 정보
    Review savedReview = Review.builder()
        .user(mockUser)
        .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
        .rating(3)
        .build();
    ReflectionTestUtils.setField(savedReview, "id",
        reviewId);                                                // NPE 방지를 위한 id 강제 주입
    ReflectionTestUtils.setField(savedReview, "status",
        ReviewStatus.ACTIVE);                                     // status 강제 주입
    ReflectionTestUtils.setField(savedReview, "likes",
        new ArrayList<>());                                       // likes 강제 주입

    given(reviewRepository.findById(reviewId)).willReturn(
        Optional.of(savedReview));                                // savedReview 반환
    given(userRepository.findById(userId)).willReturn(
        Optional.of(mockUser));                                   // mockUser 반환
    given(reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId)).willReturn(
        false);                                             // 특정 리뷰에 대한 요청자의 리뷰가 존재하지 않음

    // when
    ReviewLikeDto result = reviewServiceImplement.toggleLike(reviewId, userId);

    // then
    assertNotNull(result);
    assertTrue(
        result.liked());                                                                  // 실행 결과 확인
    assertEquals(reviewId,
        result.reviewId());                                                               // 요청 DTO 검증
    assertEquals(userId, result.userId());
    assertEquals(1,
        savedReview.getLikeCount());                                                      // 좋아요 개수
    verify(reviewLikeRepository, times(1)).
        save(
            any(ReviewLike.class));                                                      // 리뷰 좋아요 저장 함수 호출 확인
    verify(reviewRepository, times(1))
        .saveAndFlush(
            savedReview);                                                       // 리뷰 좋아요 저장 함수 호출 확인
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
    ReflectionTestUtils.setField(mockUser, "id",
        userId);                                                      // NPE 방지를 위한 id 강제 삽입

    // 좋아요를 추가할 리뷰 정보
    Review savedReview = Review.builder()
        .user(mockUser)
        .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
        .rating(3)
        .build();
    ReflectionTestUtils.setField(savedReview, "id",
        reviewId);                                                  // NPE 방지를 위한 id 강제 주입

    // 삭제할 가짜 좋아요 객체
    ReviewLike savedLike = ReviewLike.builder()
        .review(savedReview)
        .user(mockUser)
        .build();
    ReflectionTestUtils.setField(savedReview, "likeCount",
        1);                                                   // likeCount 강제 주입
    ReflectionTestUtils.setField(savedReview, "status",
        ReviewStatus.ACTIVE);                                       // status 강제 주입

    // 특정 리뷰의 좋아요 목록
    List<ReviewLike> likes = new ArrayList<>();
    likes.add(savedLike);
    ReflectionTestUtils.setField(savedReview, "likes", likes);

    given(reviewRepository.findById(reviewId)).willReturn(
        Optional.of(savedReview));                                  // savedReview 반환
    given(userRepository.findById(userId)).willReturn(
        Optional.of(mockUser));                                     // mockUser 반환
    given(reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId)).willReturn(
        true);                                                // 특정 리뷰에 대한 요청자의 리뷰가 존재하지 않음
    given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId))
        .willReturn(Optional.of(savedLike));                        // savedLike 반환

    // when
    ReviewLikeDto result = reviewServiceImplement.toggleLike(reviewId, userId);

    // then
    assertNotNull(result);
    assertFalse(
        result.liked());                                                            // 실행 결과 확인
    assertEquals(reviewId,
        result.reviewId());                                                         // 요청 DTO 검증
    assertEquals(userId, result.userId());
    assertEquals(0, savedReview.getLikeCount());
    verify(reviewLikeRepository, times(1)).delete(savedLike);
    verify(reviewRepository, times(1)).saveAndFlush(savedReview);
  }

  // [실패] 좋아요 추가 및 취소 로직이 완료되기 전 동일한 사용자로부터 같은 요청을 받은 경우, 동시성 문제 발생
  @Test
  @DisplayName("좋아요 추가 실패: 동일한 사용자로부터 똑같은 요청을 연속으로 받아 동시성 이슈가 발생한 경우, DUPLICATE_REVIEW_LIKE_REQUEST 에러 반환")
  void add_review_like_failure() {
    // given
    UUID reviewId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    // 가짜 객체 | 도서 및 사용자
    User mockUser = User.builder().build();
    ReflectionTestUtils.setField(mockUser, "id",
        userId);                                                      // NPE 방지를 위한 id 강제 삽입

    // 좋아요를 추가할 리뷰 정보
    Review savedReview = Review.builder()
        .user(mockUser)
        .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
        .rating(3)
        .build();
    ReflectionTestUtils.setField(savedReview, "id",
        reviewId);                                                     // NPE 방지를 위한 id 강제 주입
    ReflectionTestUtils.setField(savedReview, "status",
        ReviewStatus.ACTIVE);                                          // status 강제 주입
    ReflectionTestUtils.setField(savedReview, "likes",
        new ArrayList<>());

    given(reviewRepository.findById(reviewId)).willReturn(
        Optional.of(savedReview));                                    // savedReview 반환
    given(userRepository.findById(userId)).willReturn(
        Optional.of(mockUser));                                       // mockUser 반환
    given(reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId)).willReturn(
        false);                                                 // 특정 리뷰에 대한 요청자의 리뷰가 존재하지 않음

    // saveAndFlush 시점에 데이터베이스 제약 위반 예외 발생
    DataIntegrityViolationException exception = mock(DataIntegrityViolationException.class);
    Throwable cause = mock(Throwable.class);

    given(exception.getMostSpecificCause()).willReturn(cause);
    given(cause.getMessage()).willReturn(
        "Unique index or primary key violation: uk_review_user_like");        // 발생한 제약 위반 예외 = 중복 리뷰 예외
    given(reviewRepository.saveAndFlush(any(Review.class))).willThrow(
        exception);                                    // exception 반환

    // when & then
    assertThrows(DuplicateReviewLikeRequestException.class, () -> {
      // try-catch 구문 예외 반환 확인
      reviewServiceImplement.toggleLike(savedReview.getId(), mockUser.getId());
    });
  }
}
