package com.codeit.mission.deokhugam.dashboard.popularbooks.controller;

import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.popularbooks.dto.CursorPageResponsePopularBookDto;
import com.codeit.mission.deokhugam.dashboard.popularbooks.service.PopularBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books/popular")
public class PopularBookController {
  private final PopularBookService popularBookService;

  @GetMapping
  public ResponseEntity<CursorPageResponsePopularBookDto> getPopularBooks(
      @RequestParam(defaultValue = "DAILY") PeriodType period,
      @RequestParam(defaultValue = "ASC") DirectionEnum direction,
      @RequestParam(value = "cursor", required = false) String cursor,
      @RequestParam(value = "after", required = false) String after,
      @RequestParam(defaultValue = "50") int limit
  ){
    return ResponseEntity.ok(popularBookService.get(period, direction, cursor, after, limit));
  }


}
