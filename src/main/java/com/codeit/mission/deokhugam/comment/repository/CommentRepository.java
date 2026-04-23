package com.codeit.mission.deokhugam.comment.repository;

import com.codeit.mission.deokhugam.comment.entity.Comment;
import com.codeit.mission.deokhugam.dashboard.popularreviews.dto.ReviewCommentCount;
import com.codeit.mission.deokhugam.dashboard.powerusers.dto.UserCommentCount;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryCustom {

  // 리뷰의 댓글 수
  int countByReviewId(UUID reviewId);

  // 파워 유저 집계할 때 기간 별 댓글 개수를 가져오는 레포지토리 메서드
  @Query(
      """
          select new com.codeit.mission.deokhugam.dashboard.users.dto.UserCommentCount(
              c.userId,
              count(c.id)
          )
          from Comment c
          where c.createdAt >= :periodStart
            and c.createdAt < :periodEnd
          group by c.userId
          """)
  List<UserCommentCount> findUserCommentCounts(
      @Param("periodStart") LocalDateTime periodStart,
      @Param("periodEnd") LocalDateTime periodEnd);

  // 인기 리뷰를 집계할 때 기간 별 리뷰에 다린 댓글 개수를 가져오는 레포지토리 메서드
  @Query(
      """
          select new com.codeit.mission.deokhugam.dashboard.reviews.dto.ReviewCommentCount(
              c.reviewId,
              count(c.id)
          )
          from Comment c
          where c.createdAt >= :periodStart
            and c.createdAt < :periodEnd
          group by c.reviewId
          """)
  List<ReviewCommentCount> findReviewCommentCounts(
      @Param("periodStart") LocalDateTime periodStart,
      @Param("periodEnd") LocalDateTime periodEnd);


  // 사용자가 작성한 댓글들을 일괄 삭제
  @Modifying
  @Transactional
  @Query(value = "DELETE FROM comments WHERE user_id IN :userIds", nativeQuery = true)
  void deleteByUserIds(@Param("userIds") List<UUID> userIds);

  // 삭제 대상 유저들이 작성한 리뷰에 달린 모든 댓글 일괄 삭제 (서브쿼리 활용)
  @Modifying
  @Transactional
  @Query(value = "DELETE FROM comments WHERE review_id IN (SELECT id FROM reviews WHERE user_id IN :userIds)", nativeQuery = true)
  void deleteByReviewUserIds(@Param("userIds") List<UUID> userIds);

  // 삭제 대상 리뷰에 달린 댓글 일괄 삭제
  @Modifying(clearAutomatically = true)
  @Transactional
  @Query(value = "DELETE FROM Comment comment WHERE comment.reviewId IN :reviewIds")
  void deleteByReviewIdIn(@Param("reviewIds") List<UUID> reviewIds);
}
