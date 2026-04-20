package com.codeit.mission.deokhugam.comment.repository;

import com.codeit.mission.deokhugam.comment.entity.Comment;
import com.codeit.mission.deokhugam.dashboard.users.dto.UserCommentCount;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

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
public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryCustom {
}
