package com.codeit.mission.deokhugam.dashboard.powerusers.repository;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.powerusers.dto.response.PowerUserDto;
import com.codeit.mission.deokhugam.dashboard.powerusers.entity.PowerUser;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PowerUserRepository extends JpaRepository<PowerUser, UUID> {

  @Query(
      """
          select new com.codeit.mission.deokhugam.dashboard.powerusers.dto.PowerUserDto(
              pu.userId,
              u.nickname,
              pu.periodType,
              pu.createdAt,
              pu.rank,
              pu.score,
              pu.reviewScoreSum,
              pu.likeCount,
              pu.commentCount
          )
          from PowerUser pu
          join com.codeit.mission.deokhugam.user.entity.User u on u.id = pu.userId
          where pu.snapshotId = :snapshotId
            and (:cursor is null
                  or pu.rank > :cursor
                  or (pu.rank = :cursor and pu.createdAt > :after))
          order by pu.rank asc, pu.createdAt asc
          """)
  List<PowerUserDto> findRankingDtosBySnapshotIdAsc(
      @Param("snapshotId") UUID snapshotId,
      @Param("cursor") Long cursor,
      @Param("after") Instant after,
      Pageable pageable);

  @Query(
      """
          select new com.codeit.mission.deokhugam.dashboard.powerusers.dto.PowerUserDto(
              pu.userId,
              u.nickname,
              pu.periodType,
              pu.createdAt,
              pu.rank,
              pu.score,
              pu.reviewScoreSum,
              pu.likeCount,
              pu.commentCount
          )
          from PowerUser pu
          join com.codeit.mission.deokhugam.user.entity.User u on u.id = pu.userId
          where pu.snapshotId = :snapshotId
            and (:cursor is null
                  or pu.rank < :cursor
                  or (pu.rank = :cursor and pu.createdAt < :after))
          order by pu.rank desc, pu.createdAt desc
          """)
  List<PowerUserDto> findRankingDtosBySnapshotIdDesc(
      @Param("snapshotId") UUID snapshotId,
      @Param("cursor") Long cursor,
      @Param("after") Instant after,
      Pageable pageable);

  @Query(
      """
          select count(pu.id)
          from PowerUser pu
          where pu.snapshotId = :snapshotId
          """)
  long countRankingsBySnapshotId(@Param("snapshotId") UUID snapshotId);

  @Query(
      """
          select pu
          from PowerUser pu
          where pu.periodType = :periodType
            and pu.periodStart = :periodStart
            and pu.periodEnd = :periodEnd
            and pu.snapshotId = :snapshotId
          order by pu.score desc, pu.createdAt asc
          """)
  List<PowerUser> findByPeriodDescByScore(
      @Param("periodType") PeriodType periodType,
      @Param("periodStart") Instant periodStart,
      @Param("periodEnd") Instant periodEnd,
      @Param("snapshotId") UUID snapshotId);
}
