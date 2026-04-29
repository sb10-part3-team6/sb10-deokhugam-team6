package com.codeit.mission.deokhugam.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.mission.deokhugam.review.dto.request.ReviewSearchConditionDto;
import com.codeit.mission.deokhugam.review.dto.response.CursorPageResponseReviewDto;
import com.codeit.mission.deokhugam.review.dto.response.ReviewDto;
import com.codeit.mission.deokhugam.review.exception.ReviewNotFoundException;
import com.codeit.mission.deokhugam.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@WebMvcTest(ReviewController.class)
public class ReviewControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ReviewService reviewService;

  /*
      리뷰 상세 정보 조회
   */

  // [성공]
  @Test
  @DisplayName("리뷰 상세 조회 성공")
  void find_review_by_id_success() throws Exception {
    // given
    UUID bookId = UUID.randomUUID();
    UUID requestUserId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();

    // 상세 조회할 리뷰
    ReviewDto reviewDto = ReviewDto.builder()
        .id(reviewId)
        .bookId(bookId)
        .userId(requestUserId)
        .content("good")
        .likedByMe(false)
        .build();

    given(reviewService.findById(reviewId, requestUserId)).willReturn(reviewDto);

    // when & then
    mockMvc.perform(get("/api/reviews/" + reviewId)
            .header("Deokhugam-Request-User-ID", requestUserId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(reviewId.toString()))
        .andExpect(jsonPath("$.content").value("good"))
        .andExpect(jsonPath("$.likedByMe").value(false));
  }

  // [실패]
  @Test
  @DisplayName("리뷰 상세 조회 실패: 요청 헤더가 누락된 경우, 400 Bad Request 반환")
  void find_review_by_id_failure_invalid_request() throws Exception {
    // given
    UUID reviewId = UUID.randomUUID();

    // when & then
    mockMvc.perform(get("/api/reviews/" + reviewId))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.exceptionType").value("MissingRequestHeaderException"));
  }

  // [실패]
  @Test
  @DisplayName("리뷰 상세 조회 실패: 해당 리뷰가 존재하지 않을 경우, 404 Not Found 반환")
  void find_review_by_id_failure_review_not_found() throws Exception {
    // given
    UUID requestUserId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();

    given(reviewService.findById(any(UUID.class), any(UUID.class)))
        .willThrow(new ReviewNotFoundException(reviewId));

    // when & then
    mockMvc.perform(get("/api/reviews/" + reviewId)
            .header("Deokhugam-Request-User-ID", requestUserId.toString()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.exceptionType").value("ReviewNotFoundException"));
  }


  /*
      리뷰 목록 조회
   */

  // [성공]
  @Test
  @DisplayName("리뷰 목록 조회 성공")
  void find_all_review_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    // 목록 조회할 리뷰
    ReviewDto firstReview = ReviewDto.builder()
        .id(UUID.randomUUID())
        .content("good")
        .build();

    ReviewDto secondReview = ReviewDto.builder()
        .id(UUID.randomUUID())
        .content("bad")
        .build();

    CursorPageResponseReviewDto<ReviewDto> response = CursorPageResponseReviewDto.<ReviewDto>builder()
        .content(List.of(firstReview, secondReview))
        .nextCursor(String.valueOf(secondReview.id()))
        .nextAfter(Instant.now())
        .size(50)
        .hasNext(true)
        .build();

    given(reviewService.findAllByKeyword(eq(userId), any(ReviewSearchConditionDto.class)))
        .willReturn(response);

    // when & then
    mockMvc.perform(get("/api/reviews")
            .param("requestUserId", userId.toString())
            .header("Deokhugam-Request-User-ID", userId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].content").value("good"));
  }

  // [실패]
  @Test
  @DisplayName("리뷰 목록 조회 실패: 쿼리 파라미터의 유저 ID와 헤더의 유저 ID가 불일치할 경우, 400 Bad Request 반환")
  void find_all_review_failure_mismatch() throws Exception {
    // given
    UUID queryUserId = UUID.randomUUID();
    UUID headerUserId = UUID.randomUUID();

    // when & then
    mockMvc.perform(get("/api/reviews")
            .param("requestUserId", queryUserId.toString())
            .header("Deokhugam-Request-User-ID", headerUserId.toString()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.exceptionType").value("RequestUserMismatchException"));
  }


  /*
      리뷰 등록
   */


  /*
      리뷰 수정
   */


  /*
      리뷰 논리 삭제
   */


  /*
      리뷰 물리 삭제
   */


  /*
      리뷰 좋아요 및 취소
   */
}
