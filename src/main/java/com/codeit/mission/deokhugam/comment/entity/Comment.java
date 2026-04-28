package com.codeit.mission.deokhugam.comment.entity;

import com.codeit.mission.deokhugam.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "comments")
public class Comment extends BaseEntity {

  // 리뷰 정보
  @Column(name = "review_id", nullable = false)
  private UUID reviewId;

  // 사용자 정보
  @Column(name = "user_id", nullable = false)
  private UUID userId;

  // 댓글 내용
  @Column(name = "content", nullable = false, length = 500)
  private String content;

  // 댓글 상태 - 논리 삭제 구분 용
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private CommentStatus status;

  @Builder
  public Comment(UUID reviewId, UUID userId, String content, CommentStatus status) {
    super();
    this.reviewId = Objects.requireNonNull(reviewId);
    this.userId = Objects.requireNonNull(userId);
    validateContent(content);
    this.content = content;
    this.status = Objects.requireNonNull(status);
  }

  // 댓글 수정
  public void updateContent(String content) {
    validateContent(content);
    this.content = content;
  }

  // 댓글 상태 변경
  public void updateStatus(CommentStatus status) {
    Objects.requireNonNull(status);
    this.status = status;
  }

  // 댓글 내용 유효성 검사
  private void validateContent(String content) {
    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("댓글 내용은 비워둘 수 없습니다.");
    }
    if (content.length() > 500) {
      throw new IllegalArgumentException("댓글 내용은 500자 이하로 작성해 주세요.");
    }
  }
}
