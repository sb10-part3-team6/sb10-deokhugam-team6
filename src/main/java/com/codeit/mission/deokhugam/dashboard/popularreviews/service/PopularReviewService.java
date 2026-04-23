package com.codeit.mission.deokhugam.dashboard.popularreviews.service;

import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.popularreviews.dto.CursorPageResponsePopularReviewDto;
import com.codeit.mission.deokhugam.dashboard.popularreviews.repository.PopularReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// 조회 서비스
// 추후 구현 예정
@Service
@RequiredArgsConstructor
public class PopularReviewService {

  private final PopularReviewRepository popularReviewRepository;

  public CursorPageResponsePopularReviewDto getReviews(
      PeriodType periodType,
      DirectionEnum direction,
      String cursor,
      String after,
      int size
  ) {
    throw new UnsupportedOperationException("Popular Review 조회는 아직 구현되지 않았습니다.");
  }
}