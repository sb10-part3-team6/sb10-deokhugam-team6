package com.codeit.mission.deokhugam.review.controller;

import com.codeit.mission.deokhugam.review.dto.request.ReviewCreateRequest;
import com.codeit.mission.deokhugam.review.dto.request.ReviewSearchConditionDto;
import com.codeit.mission.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.codeit.mission.deokhugam.review.dto.response.CursorPageResponseReviewDto;
import com.codeit.mission.deokhugam.review.dto.response.ReviewDto;
import com.codeit.mission.deokhugam.review.dto.response.ReviewLikeDto;
import com.codeit.mission.deokhugam.review.exception.RequestUserMismatchException;
import com.codeit.mission.deokhugam.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/*
    리뷰 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

  private final ReviewService reviewService;

  // 리뷰 상세 조회
  @GetMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> findById(@PathVariable UUID reviewId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
    ReviewDto response = reviewService.findById(reviewId, requestUserId);

    return ResponseEntity.ok(response);
  }

  // 리뷰 목록 조회
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

  // 리뷰 등록
  @PostMapping
  public ResponseEntity<ReviewDto> create(
      @Valid @RequestBody ReviewCreateRequest reviewCreateRequest) {
    ReviewDto response = reviewService.create(reviewCreateRequest);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // 리뷰 수정
  @PatchMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> update(@PathVariable UUID reviewId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
      @Valid @RequestBody ReviewUpdateRequest reviewUpdateRequest) {
    ReviewDto response = reviewService.update(reviewId, requestUserId, reviewUpdateRequest);

    return ResponseEntity.ok(response);
  }

  // 리뷰 논리 삭제
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> delete(@PathVariable UUID reviewId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
    reviewService.delete(reviewId, requestUserId);

    return ResponseEntity.noContent().build();
  }

  // 리뷰 물리 삭제
  @DeleteMapping("/{reviewId}/hard")
  public ResponseEntity<Void> hardDelete(@PathVariable UUID reviewId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
    reviewService.hardDelete(reviewId, requestUserId);

    return ResponseEntity.noContent().build();
  }

  // 리뷰 좋아요 추가 및 취소
  @PostMapping("/{reviewId}/like")
  public ResponseEntity<ReviewLikeDto> toggleLike(@PathVariable UUID reviewId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
    ReviewLikeDto response = reviewService.toggleLike(reviewId, requestUserId);

    return ResponseEntity.ok(response);
  }
}
