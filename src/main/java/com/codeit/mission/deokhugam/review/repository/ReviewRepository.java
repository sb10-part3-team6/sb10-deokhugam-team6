package com.codeit.mission.deokhugam.review.repository;

import com.codeit.mission.deokhugam.dashboard.users.dto.UserReviewAggregate;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.entity.ReviewStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

  // 유저 Id별 리뷰의 점수 총계를 리턴하는 메서드
  @Query(
      """
      select new com.codeit.mission.deokhugam.dashboard.users.dto.UserReviewAggregate(
          r.user.id,
          coalesce(sum(r.rating), 0)
      )
      from Review r
      where r.createdAt >= :periodStart
        and r.createdAt < :periodEnd
        and r.status = :status
      group by r.user.id
      """)
  List<UserReviewAggregate> findUserReviewAggregates(
      @Param("periodStart") LocalDateTime periodStart,
      @Param("periodEnd") LocalDateTime periodEnd,
      @Param("status") ReviewStatus status);
}
