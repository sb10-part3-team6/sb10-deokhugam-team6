package com.codeit.mission.deokhugam.dashboard.users.repository;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.users.dto.PowerUserDto;
import com.codeit.mission.deokhugam.dashboard.users.entity.PowerUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PowerUserRepository extends JpaRepository<PowerUser, UUID> {

  void deleteByPeriodTypeAndPeriodStartAndPeriodEnd(
      PeriodType periodType, LocalDateTime periodStart, LocalDateTime periodEnd);

  // 파워 유저 집계 테이블에서 기간(Period)에 따른 파워 유저들을 오름차순으로 Fetch해오는 쿼리
  @Query(
      """
      select new com.codeit.mission.deokhugam.dashboard.users.dto.PowerUserDto(
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
            where pu.periodType = :periodType
        and (:cursor is null
              or pu.rank > :cursor 
              or (pu.rank = :cursor and pu.createdAt > :after))
        and pu.periodStart = (
          select max(p2.periodStart)
          from PowerUser p2
          where p2.periodType = :periodType
        )
      order by pu.rank asc, pu.createdAt asc
      """)
  // 쿼리 설명
  /*
    1. 조회 결과를 바로 PowerUserDto 형식으로 반환한다. (프로젝션)
    2. 기준테이블은 PowerUser 테이블이며, 커서 페이지네이션 응답에 nickname이 필요하므로, user 테이블 조인
    3. :periodType 에 해당하는 로우들만 추출한다.
    4. 커서가 존재하지 않으면 첫 페이지를 반환
    5. 커서가 존재하면 rank > cursor 조건을 적용하여 해당 rank 이후의 데이터만 조회한다.
        - 또는 rank == cursor이면서 createdAt > after 인 데이터들만 조회한다
    6. periodStart (집계시작일자) 를 기준으로 가장 최신 파워 유저들만 조회함.
    7. rank를 오름차 순으로 정렬한다
   */
  List<PowerUserDto> findLatestRankingDtosByPeriodTypeAsc(
      @Param("periodType") PeriodType periodType,
      @Param("cursor") Long cursor,
      @Param("after") LocalDateTime after,
      Pageable pageable); // 여기서 이 Pageable은 size + 1 을 조회하기 위한 역할

  // 파워 유저 집계 테이블에서 기간(Period)에 따른 파워 유저들을 오름차순으로 Fetch해오는 쿼리
  @Query(
      """
      select new com.codeit.mission.deokhugam.dashboard.users.dto.PowerUserDto(
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
            where pu.periodType = :periodType
        and (:cursor is null
              or pu.rank < :cursor
              or (pu.rank = :cursor and pu.createdAt < :after))
        and pu.periodStart = (
          select max(p2.periodStart)
          from PowerUser p2
          where p2.periodType = :periodType
        )
      order by pu.rank desc, pu.createdAt desc
      """)
  List<PowerUserDto> findLatestRankingDtosByPeriodTypeDesc(
      @Param("periodType") PeriodType periodType,
      @Param("cursor") Long cursor,
      @Param("after") LocalDateTime after,
      Pageable pageable);

  // periodType 별 파워 유저들의 개수(count)를 반환하는 쿼리
  @Query(
      """
      select count(pu.id)
      from PowerUser pu
      where pu.periodType = :periodType
        and pu.periodStart = (
          select max(p2.periodStart)
          from PowerUser p2
          where p2.periodType = :periodType
        )
      """)
  long countLatestRankingsByPeriodType(@Param("periodType") PeriodType periodType);
}
