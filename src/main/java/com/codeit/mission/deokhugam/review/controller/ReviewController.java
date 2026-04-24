package com.codeit.mission.deokhugam.review.controller;

import com.codeit.mission.deokhugam.review.dto.request.ReviewCreateRequest;
import com.codeit.mission.deokhugam.review.dto.request.ReviewSearchConditionDto;
import com.codeit.mission.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.codeit.mission.deokhugam.review.dto.response.CursorPageResponseReviewDto;
import com.codeit.mission.deokhugam.review.dto.response.ReviewDto;
import com.codeit.mission.deokhugam.review.dto.response.ReviewLikeDto;
import com.codeit.mission.deokhugam.review.exception.RequestUserMismatchException;
import com.codeit.mission.deokhugam.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "리뷰 관리", description = "리뷰 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

  private final ReviewService reviewService;

  @Operation(
      summary = "리뷰 상세 정보 조회",
      operationId = "find_5",
      description = "리뷰 ID로 상세 정보를 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "리뷰 정보 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)"),
      @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> findById(@PathVariable UUID reviewId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
    ReviewDto response = reviewService.findById(reviewId, requestUserId);

    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "리뷰 목록 조회",
      operationId = "find_all_5",
      description = "검색 조건에 맞는 리뷰 목록을 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류, 요청자 ID 누락)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping
  public ResponseEntity<CursorPageResponseReviewDto<ReviewDto>> findAll(
      @RequestParam UUID requestUserId,                                         // 쿼리 파리미터
      @RequestHeader("Deokhugam-Request-User-ID") UUID headerRequestUserId,     // 헤더 정보 (별도 사용 X)
      @ModelAttribute ReviewSearchConditionDto reviewSearchConditionDto) {      // 검색 조건 DTO
    // 쿼리로 들어온 요청자와 헤더로 들어온 요청자가 불일치 하는 경우
    if (!requestUserId.equals(headerRequestUserId)) {
      throw new RequestUserMismatchException(requestUserId, headerRequestUserId);
    }

    CursorPageResponseReviewDto<ReviewDto> response = reviewService.findAllByKeyword(requestUserId,
        reviewSearchConditionDto);

    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "리뷰 등록",
      operationId = "create_5",
      description = "새로운 리뷰를 등록합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "리뷰 등록 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
      @ApiResponse(responseCode = "409", description = "이미 작성된 리뷰 존재"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping
  public ResponseEntity<ReviewDto> create(
      @Valid @RequestBody ReviewCreateRequest reviewCreateRequest) {
    ReviewDto response = reviewService.create(reviewCreateRequest);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(
      summary = "리뷰 수정",
      operationId = "update_5",
      description = "본인이 작성한 리뷰를 수정합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "403", description = "리뷰 수정 권한 없음"),
      @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PatchMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> update(@PathVariable UUID reviewId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
      @Valid @RequestBody ReviewUpdateRequest reviewUpdateRequest) {
    ReviewDto response = reviewService.update(reviewId, requestUserId, reviewUpdateRequest);

    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "리뷰 논리 삭제",
      operationId = "logical_delete_5",
      description = "본인이 작성한 리뷰를 논리적으로 삭제합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)"),
      @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음"),
      @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> delete(@PathVariable UUID reviewId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
    reviewService.delete(reviewId, requestUserId);

    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "리뷰 물리 삭제",
      operationId = "hard_delete_5",
      description = "본인이 작성한 리뷰를 물리적으로 삭제합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)"),
      @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음"),
      @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/{reviewId}/hard")
  public ResponseEntity<Void> hardDelete(@PathVariable UUID reviewId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
    reviewService.hardDelete(reviewId, requestUserId);

    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "리뷰 좋아요",
      operationId = "toggle_like_5",
      description = "리뷰에 좋아요를 추가하거나 취소합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "리뷰 좋아요 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)"),
      @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
      @ApiResponse(responseCode = "409", description = "이미 생성된 좋아요 존재"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping("/{reviewId}/like")
  public ResponseEntity<ReviewLikeDto> toggleLike(@PathVariable UUID reviewId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
    ReviewLikeDto response = reviewService.toggleLike(reviewId, requestUserId);

    return ResponseEntity.ok(response);
  }
}
