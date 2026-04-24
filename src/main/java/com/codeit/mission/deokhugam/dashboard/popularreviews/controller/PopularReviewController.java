package com.codeit.mission.deokhugam.dashboard.popularreviews.controller;

import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.popularreviews.dto.CursorPageResponsePopularReviewDto;
import com.codeit.mission.deokhugam.dashboard.popularreviews.service.PopularReviewService;
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
  public ResponseEntity<CursorPageResponsePopularReviewDto> getPopularReviews(
      @RequestParam(defaultValue = "DAILY") PeriodType period,
      @RequestParam(defaultValue = "ASC") DirectionEnum direction,
      @RequestParam(value = "cursor", required = false) String cursor,
      @RequestParam(value = "after", required = false) String after,
      @RequestParam(defaultValue = "50") int limit
  ) {
    return ResponseEntity.ok(popularReviewService.getReviews(period, direction, cursor, after, limit));
  }

}
