package com.codeit.mission.deokhugam.dashboard.reviews.service;

import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.reviews.dto.CursorPageResponsePopularReviewDto;
import com.codeit.mission.deokhugam.dashboard.reviews.repository.PopularReviewRepository;
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
  ){
//    Objects.requireNonNull(periodType, "PeriodType is invalid");
//    Objects.requireNonNull(periodType, "Direction is invalid");
//    if ((cursor == null) != (after == null)) {
//      throw new CursorAfterNotProvidedTogetherException();
//    }
//


  }

}
