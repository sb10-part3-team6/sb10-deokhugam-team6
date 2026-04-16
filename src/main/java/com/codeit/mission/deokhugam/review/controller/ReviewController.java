package com.codeit.mission.deokhugam.review.controller;

import com.codeit.mission.deokhugam.review.dto.request.ReviewCreateRequest;
import com.codeit.mission.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.codeit.mission.deokhugam.review.dto.response.ReviewDto;
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
public class ReviewController {
    private final ReviewService reviewService;

    // 리뷰 상세 조회
    @GetMapping("/api/reviews/{reviewId}")
    public ResponseEntity<ReviewDto> findById(@PathVariable UUID reviewId,
                                              @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
        ReviewDto response = reviewService.findById(reviewId, requestUserId);

        return ResponseEntity.ok(response);
    }

    // 리뷰 목록 조회

    // 리뷰 등록
    @PostMapping("/api/reviews")
    public ResponseEntity<ReviewDto> create(@Valid @RequestBody ReviewCreateRequest reviewCreateRequest) {
        ReviewDto response = reviewService.create(reviewCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 리뷰 수정
    @PatchMapping("/api/reviews/{reviewId}")
    public ResponseEntity<ReviewDto> update(@PathVariable UUID reviewId,
                                            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
                                            @Valid @RequestBody ReviewUpdateRequest reviewUpdateRequest) {
        ReviewDto response = reviewService.update(reviewId, requestUserId, reviewUpdateRequest);

        return ResponseEntity.ok(response);
    }

    // 리뷰 논리 삭제
    // 리뷰 물리 삭제

    // 리뷰 좋아요
}
