package com.codeit.mission.deokhugam.dashboard.popularreviews.repository;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.popularreviews.dto.PopularReviewDto;
import com.codeit.mission.deokhugam.dashboard.popularreviews.entity.PopularReview;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
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
  // period 기간 내에 존재하는 PopularReview를 점수 순으로 내림차순 하여 뽑는다
  List<PopularReview> findByPeriodDescByScore(
      @Param("periodType") PeriodType periodType,
      @Param("periodStart") LocalDateTime periodStart,
      @Param("periodEnd") LocalDateTime periodEnd,
      @Param("snapshotId") UUID snapshotId);

  @Query(
      """
      select new com.codeit.mission.deokhugam.dashboard.popularreviews.dto.PopularReviewDto(
          pr.id,
          r.id,
          b.id,
          b.title,
          b.thumbnailUrl,
          u.id,
          u.nickname,
          r.content,
          r.rating * 1.0,
          pr.periodType,
          pr.createdAt,
          pr.rank,
          pr.score,
          pr.likeCount,
          pr.commentCount
      )
      from PopularReview pr
      join com.codeit.mission.deokhugam.review.entity.Review r on r.id = pr.reviewId
      join r.book b
      join r.user u
      where pr.snapshotId = :snapshotId
        and (:cursor is null
              or pr.rank > :cursor
              or (pr.rank = :cursor and pr.createdAt > :after))
      order by pr.rank asc, pr.createdAt asc
      """)
  // book, user, review를 join하여 PopularReviewDto로 만드는 쿼리 메서드
  List<PopularReviewDto> findRankingDtosBySnapshotIdAsc(
      @Param("snapshotId") UUID snapshotId,
      @Param("cursor") Long cursor,
      @Param("after") LocalDateTime after,
      Pageable pageable);

  @Query(
      """
      select new com.codeit.mission.deokhugam.dashboard.popularreviews.dto.PopularReviewDto(
          pr.id,
          r.id,
          b.id,
          b.title,
          b.thumbnailUrl,
          u.id,
          u.nickname,
          r.content,
          r.rating * 1.0,
          pr.periodType,
          pr.createdAt,
          pr.rank,
          pr.score,
          pr.likeCount,
          pr.commentCount
      )
      from PopularReview pr
      join com.codeit.mission.deokhugam.review.entity.Review r on r.id = pr.reviewId
      join r.book b
      join r.user u
      where pr.snapshotId = :snapshotId
        and (:cursor is null
              or pr.rank < :cursor
              or (pr.rank = :cursor and pr.createdAt < :after))
      order by pr.rank desc, pr.createdAt desc
      """)
  List<PopularReviewDto> findRankingDtosBySnapshotIdDesc(
      @Param("snapshotId") UUID snapshotId,
      @Param("cursor") Long cursor,
      @Param("after") LocalDateTime after,
      Pageable pageable);

  @Query(
      """
      select count(pr.id)
      from PopularReview pr
      where pr.snapshotId = :snapshotId
      """)
  long countRankingsBySnapshotId(@Param("snapshotId") UUID snapshotId);

}
