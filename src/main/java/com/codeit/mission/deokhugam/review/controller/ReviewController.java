package com.codeit.mission.deokhugam.review.controller;

import com.codeit.mission.deokhugam.review.dto.request.ReviewCreateRequest;
import com.codeit.mission.deokhugam.review.dto.response.ReviewDto;
import com.codeit.mission.deokhugam.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
    리뷰 컨트롤러
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    // 리뷰 상세 조회
    // 리뷰 목록 조회

    // 리뷰 등록
    @PostMapping
    public ResponseEntity<ReviewDto> create(@Valid @RequestBody ReviewCreateRequest reviewCreateRequest) {
        ReviewDto reviewDto = reviewService.create(reviewCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(reviewDto);
    }

    // 리뷰 수정

    // 리뷰 논리 삭제
    // 리뷰 물리 삭제

    // 리뷰 좋아요
}
