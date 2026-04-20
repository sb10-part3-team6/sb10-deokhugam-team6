package com.codeit.mission.deokhugam.dashboard.users.repository;

import com.codeit.mission.deokhugam.dashboard.users.dto.PowerUserLikeCount;
import com.codeit.mission.deokhugam.dashboard.users.entity.ReviewLike;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {

  @Query(
      """
      select new com.codeit.mission.deokhugam.dashboard.users.dto.PowerUserLikeCount(
          rl.userId,
          count(rl.id)
      )
      from ReviewLike rl
      where rl.createdAt >= :periodStart
        and rl.createdAt < :periodEnd
      group by rl.userId
      """)
  List<PowerUserLikeCount> findUserLikeCounts(
      @Param("periodStart") LocalDateTime periodStart,
      @Param("periodEnd") LocalDateTime periodEnd);
}
