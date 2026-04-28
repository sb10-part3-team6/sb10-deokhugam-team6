package com.codeit.mission.deokhugam.dashboard.popularbooks.controller;

import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.popularbooks.dto.response.CursorPageResponsePopularBookDto;
import com.codeit.mission.deokhugam.dashboard.popularbooks.service.PopularBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "대시보드 관리", description = "대시보드 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books/popular")
public class PopularBookController {

  private final PopularBookService popularBookService;

  @Operation(
      summary = "인기 도서 목록 조회",
      operationId = "find_popular_book_3",
      description = "기간별 인기 도서 목록을 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "인기 독서 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (랭킹 기간 오류, 정렬 방향 오류 등)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping
  public ResponseEntity<CursorPageResponsePopularBookDto> getPopularBooks(
      @RequestParam(defaultValue = "DAILY") PeriodType period,
      @RequestParam(defaultValue = "ASC") DirectionEnum direction,
      @RequestParam(value = "cursor", required = false) String cursor,
      @RequestParam(value = "after", required = false) String after,
      @RequestParam(defaultValue = "50") int limit
  ) {
    return ResponseEntity.ok(popularBookService.get(period, direction, cursor, after, limit));
  }


}
