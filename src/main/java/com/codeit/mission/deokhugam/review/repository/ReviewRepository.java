package com.codeit.mission.deokhugam.review.repository;

import com.codeit.mission.deokhugam.dashboard.users.dto.UserReviewAggregate;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.entity.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/*
    리뷰 레파지토리
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

  // 중복 검사: 특정 도서에 대한 사용자 리뷰 존재 유무
  boolean existsByBookIdAndUserId(UUID bookId, UUID userId);

  // 좋아요 수 증가
  @Modifying
  @Query("UPDATE Review review SET review.likeCount = review.likeCount + 1 " +
          "WHERE review.id = :id")
  void incrementLikes(@Param("id") UUID id);

  // 좋아요 수 감소
  @Modifying
  @Query("UPDATE Review review SET review.likeCount = review.likeCount - 1 " +
          "WHERE review.id = :id")
  void decrementLikes(@Param("id") UUID id);

  // 좋아요 삭제
  @Modifying
  @Query(value = "DELETE FROM review_likes " +
                 "WHERE review_id = :reviewId AND user_id = :userId",
          nativeQuery = true)
  int deleteReviewLike(@Param("reviewId") UUID reviewId, @Param("userId") UUID userId);

  // 특정 리뷰에 대한 특정 유저의 좋아요 여부
  @Query("SELECT COUNT(review.id) > 0 " +                                         // 조건 만족 여부에 따라, true / false 반환
          "FROM Review review " +
          "JOIN review.likedUsers user " +                                        // Review 엔티티 내부 likedUser 필드 조인
          "WHERE review.id = :reviewId AND user.id = :userId")                    // 리뷰 id 및 사용자 id에 대한 완전 일치 조건
  boolean existsLikedByIdAndUserId(@Param("reviewId") UUID reviewId,
                                   @Param("userId") UUID userId);

  // 유저 Id별 리뷰의 점수 총계를 리턴하는 메서드
  @Query(
      """
          select new com.codeit.mission.deokhugam.dashboard.users.dto.UserReviewAggregate(
              r.user.id,
              coalesce(sum(r.rating), 0.0)
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

  // 특정 리뷰에 대한 특정 유저의 좋아요 여부
  @Query("SELECT COUNT(review.id) > 0 " +// 조건 만족 여부에 따라, true / false 반환
      "FROM Review review " +
      "JOIN review.likedUsers user " +
      // Review 엔티티 내부 likedUser 필드 조인
      "WHERE review.id = :reviewId AND user.id = :userId")
  // 리뷰 id 및 사용자 id에 대한 완전 일치 조건
  boolean existsLikedByIdAndUserId(@Param("reviewId") UUID reviewId,
      @Param("userId") UUID userId);

  // 사용자가 누른 '좋아요' 기록들을 일괄 삭제 (외래 키 제약 조건 해결용)
  @Modifying
  @Transactional
  @Query(value = "DELETE FROM review_likes WHERE user_id IN :userIds", nativeQuery = true)
  void deleteLikesByUserIds(@Param("userIds") List<UUID> userIds);

  // 삭제 대상 유저들이 작성한 리뷰 ID 목록 조회
  @Query(value = "SELECT id FROM reviews WHERE user_id IN :userIds", nativeQuery = true)
  List<UUID> findReviewIdsByUserIds(@Param("userIds") List<UUID> userIds);

  // 특정 리뷰 ID들에 달린 모든 좋아요 삭제 (외래 키 제약 조건 해결용)
  @Modifying
  @Transactional
  @Query(value = "DELETE FROM review_likes WHERE review_id IN :reviewIds", nativeQuery = true)
  void deleteLikesByReviewIds(@Param("reviewIds") List<UUID> reviewIds);

  @Modifying
  @Transactional
  @Query(value = "DELETE FROM reviews WHERE user_id IN :userIds", nativeQuery = true)
  void deleteByUserIds(@Param("userIds") List<UUID> userIds);
}
