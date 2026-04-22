package com.codeit.mission.deokhugam.review.repository;

import com.codeit.mission.deokhugam.review.dto.request.ReviewSearchConditionDto;
import com.codeit.mission.deokhugam.review.entity.Review;
import java.util.List;

/*
  ReviewRepository
  ----------------------------
  정렬 및 페이지네이션에 활용할 Query DSL 전용 인터페이스 선언
 */
public interface ReviewRepositoryCustom {

  // 필터링 + 정렬 + 커서 기반 페이지네이션이 적용된 연동 목록 조회
  List<Review> searchReviews(ReviewSearchConditionDto condition);

  // 필터링 조건이 적용된 연동 목록의 전체 개수 조회
  long countWithFilter(ReviewSearchConditionDto condition);
}
