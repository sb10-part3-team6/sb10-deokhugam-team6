package com.codeit.mission.deokhugam.dashboard.reviews.controller;

import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.reviews.dto.CursorPageResponsePopularReviewDto;
import com.codeit.mission.deokhugam.dashboard.reviews.service.PopularReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews/popular")
public class PopularReviewController {

  private final PopularReviewService popularReviewService;

  @GetMapping
  ResponseEntity<CursorPageResponsePopularReviewDto> getPopularReviews(
      @RequestParam("period") PeriodType periodType,
      @RequestParam("direction") DirectionEnum direction,
      @RequestParam("cursor") String cursor,
      @RequestParam("after") String after,
      @RequestParam("limit") int limit
  ){

    return ResponseEntity.ok(popularReviewService.getReviews(periodType, direction,
        cursor, after, limit));

  }

}
