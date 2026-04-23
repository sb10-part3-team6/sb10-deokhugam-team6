package com.codeit.mission.deokhugam.dashboard.popularreviews.repository;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.popularreviews.entity.PopularReview;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID> {
  @Query("""
      select pr
            from PopularReview pr
            where pr.periodType = :periodType
                  and pr.periodStart = :periodStart
                  and pr.periodEnd = :periodEnd
                  and pr.snapshotId = :snapshotId
            order by pr.score desc, pr.createdAt asc
      """)
  List<PopularReview> findByPeriodDescByScore(
      @Param("periodType") PeriodType periodType,
      @Param("periodStart") LocalDateTime periodStart,
      @Param("periodEnd") LocalDateTime periodEnd,
      @Param("snapshotId") UUID snapshotId);

}
