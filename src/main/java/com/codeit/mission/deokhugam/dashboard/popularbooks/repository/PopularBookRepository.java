package com.codeit.mission.deokhugam.dashboard.popularbooks.repository;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.popularbooks.entity.PopularBook;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PopularBookRepository extends JpaRepository<PopularBook, UUID> {


  @Query("""
    select pb
        from PopularBook pb
            where pb.periodType = :periodType
            and pb.snapshotId = :snapshotId
            and pb.periodStart >= :periodStart
            and pb.periodEnd < :periodEnd
        order by pb.score desc
    """)
  List<PopularBook> findByPeriodAndSnapshotIdDescByScore(
      @Param("periodType") PeriodType periodType,
      @Param("periodStart") LocalDateTime periodStart,
      @Param("periodEnd") LocalDateTime periodEnd,
      @Param("snapshotId") UUID snapshotId
      );


}
