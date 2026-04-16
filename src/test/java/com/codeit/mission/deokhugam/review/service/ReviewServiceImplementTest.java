package com.codeit.mission.deokhugam.review.service;

import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.review.dto.request.ReviewCreateRequest;
import com.codeit.mission.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.codeit.mission.deokhugam.review.dto.response.ReviewDto;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.exception.ReviewAuthorMismatchException;
import com.codeit.mission.deokhugam.review.exception.ReviewNotFoundException;
import com.codeit.mission.deokhugam.review.mapper.ReviewMapper;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ReviewServiceImplementTest {
    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

//    @Mock
//    private BookRepository bookRepository;

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
        ReflectionTestUtils.setField(requestUser, "id", requestUserId);

        // 조회할 리뷰
        Review savedReview = Review.builder()
                .content("meow meow")
                .rating(5)
                .build();
        ReflectionTestUtils.setField(savedReview, "id", reviewId);

        // 내부 로직 흐름 설정
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));                    // savedReview 반환
        given(userRepository.findById(requestUserId)).willReturn(Optional.of(requestUser));                 // requestUser 반환
        given(reviewRepository.existsByIdAndUserId(reviewId, requestUserId)).willReturn(true);        // 특정 리뷰에 대한 사용자의 좋아요 여부

        // 응답 DTO 객체
        ReviewDto expectedReviewDto = ReviewDto.builder()
                .content(savedReview.getContent())
                .rating(savedReview.getRating())
                .likedByMe(true)
                .build();
        given(reviewMapper.toDto(any(Review.class), anyBoolean())).willReturn(expectedReviewDto);           // exceptedReviewDto 반환

        // when
        ReviewDto result = reviewServiceImplement.findById(reviewId, requestUserId);

        // then
        assertNotNull(result);                                                          // 상세 조회 성공 여부
        assertEquals(expectedReviewDto.content(), result.content());                    // 가짜 DTO 객체와 실제 조회 결과 비교
        assertTrue(result.likedByMe());                                                 // 좋아요 여부 반영 확인
    }

    // [실패]
    @Test
    @DisplayName("리뷰 상세 조회 완료")
    void find_review_by_id_failure() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();

        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // when & then
        ReviewNotFoundException exception = assertThrows(ReviewNotFoundException.class,
                () -> {
                    // validateOwner 예외 반환 확인
                    reviewServiceImplement.findById(reviewId, requestUserId);
                });
        verify(reviewRepository, never()).existsByIdAndUserId(any(), any());        // Repository의 유효성 검증 (중복 검사) 미호출 확인
        verify(reviewMapper, never()).toDto(any(), anyBoolean());                   // Mapper의 toDto 미호출 확인
    }

    /*
        리뷰 등록
        -------
        현재 미구현 상태, 도서 (Book) 코드가 올라오면 진행할 예정
     */

    // [성공]
    @Test
    @DisplayName("리뷰 생성 완료")
    void create_review_success() throws Exception {
        // given

        // 생성할 리뷰
        ReviewCreateRequest createRequest = new ReviewCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "고양이가 의젓하게 상점 운영도 하고 정말 귀엽네요",
                4
        );

        // when

        // then
    }

    /*
        리뷰 수정
     */

    // [성공]
    @Test
    @DisplayName("리뷰 수정 완료")
    void update_review_success() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // 가짜 객체 | 도서 및 사용자
        Book mockBook = Book.builder().build();
        User mockUser = User.builder().build();
        ReflectionTestUtils.setField(mockUser, "id", userId);               // NPE 방지를 위한 id 강제 삽입

        // 기존 리뷰 정보
        Review savedReview = Review.builder()
                .book(mockBook)
                .user(mockUser)
                .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
                .rating(3)
                .build();
        ReflectionTestUtils.setField(savedReview, "id", reviewId);

        // 수정할 리븊 내용
        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(
                "나도 선량한 지구인인데 왜 로키를 만날 수 없는 거지. 질문.",
                5
        );

        // 내부 로직 흐름 설정
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));            // savedReview 반환
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));                   // mockUser 반환
        given(reviewRepository.existsByIdAndUserId(reviewId, userId)).willReturn(false);      // 특정 리뷰에 대한 사용자의 좋아요 여부

        // when
        reviewServiceImplement.update(reviewId, userId, updateRequest);

        // then
        assertEquals(savedReview.getContent(), updateRequest.content());            // 리뷰 내용 변경 확인
        assertEquals(savedReview.getRating(), updateRequest.rating());              // 평점 변경 확인
    }

    // [실패] 요청자와 리뷰 작성자 불일치
    @Test
    @DisplayName("리뷰 수정 실패: 요청자와 리뷰 작성자가 불일치 할 경우, REVIEW_AUTHOR_MISMACHT 에러 반환")
    void update_review_failure() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();

        // 가짜 객체
        User author = User.builder().build();                                     // 가짜 작성자 객체
        ReflectionTestUtils.setField(author, "id", userId);                 // NPE 방지를 위한 id 강제 삽입
        User requestUser = User.builder().build();                                // 가짜 요청자 객체
        ReflectionTestUtils.setField(requestUser, "id", requestUserId);

        // 기존 리뷰 정보
        Review savedReview = Review.builder()
                .user(author)
                .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
                .rating(3)
                .build();
        ReflectionTestUtils.setField(savedReview, "id", reviewId);

        // 수정할 리븊 내용
        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(
                "나도 선량한 지구인인데 왜 로키를 만날 수 없는 거지. 질문.",
                5
        );

        // 내부 로직 흐름 설정
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));          // savedReview 반환
        given(userRepository.findById(userId)).willReturn(Optional.of(author));                   // author 반환
        given(userRepository.findById(requestUserId)).willReturn(Optional.of(requestUser));       // requestUser 반환

        // when & then
        assertThrows(ReviewAuthorMismatchException.class, () -> {
            // validateOwner 예외 반환 확인
            reviewServiceImplement.update(reviewId, requestUserId, updateRequest);
        });
        verify(reviewRepository, never()).save(any(Review.class));                      // Repository의 save 함수 미호출 확인
        verify(reviewMapper, never()).toDto(any(Review.class), anyBoolean());           // Mapper의 toDto 함수 미호출 확인
    }
}
