package com.codeit.mission.deokhugam.comment.repository;

import com.codeit.mission.deokhugam.comment.entity.Comment;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

  // 특정 기간 이후에 생성된 댓글의 개수를 카운트하기 위한 레포지토리 메서드
  long countByUserIdAndCreatedAtGreaterThanEqual(UUID userId, LocalDateTime createdAt);
}

