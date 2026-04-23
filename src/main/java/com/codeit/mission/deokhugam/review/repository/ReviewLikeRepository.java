package com.codeit.mission.deokhugam.review.repository;

import com.codeit.mission.deokhugam.dashboard.powerusers.dto.PowerUserLikeCount;
import com.codeit.mission.deokhugam.review.entity.ReviewLike;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {

  @Query(
      """
          select new com.codeit.mission.deokhugam.dashboard.users.dto.PowerUserLikeCount(
              rl.user.id,
              count(rl)
          )
          from ReviewLike rl
          where rl.review.status = com.codeit.mission.deokhugam.review.entity.ReviewStatus.ACTIVE
                and rl.likedAt >= :periodStart
                and rl.likedAt < :periodEnd
          group by rl.user.id
          """
  )
  List<PowerUserLikeCount> findUserLikeCounts(
      @Param("periodStart") LocalDateTime periodStart,
      @Param("periodEnd") LocalDateTime periodEnd);

  // 리뷰 ID 및 사용자 ID를 통한 리뷰 좋아요 조회
  Optional<ReviewLike> findByReviewIdAndUserId(UUID reviewId, UUID userId);

  // 특정 리뷰에 대한 특정 유저의 좋아요 여부
  boolean existsByReviewIdAndUserId(UUID reviewId, UUID userId);

  // 특정 사용자가 좋아요를 누른 리뷰 목록 조회
  @Query("SELECT reviewLike.review.id " +
      "FROM ReviewLike reviewLike " +
      "WHERE reviewLike.user.id = :userId AND reviewLike.review.id IN :reviewIds")
  List<UUID> findReviewIdsByUserIdAndReviewIdIn(@Param("userId") UUID userId,
      @Param("reviewIds") List<UUID> reviewIds);
}
