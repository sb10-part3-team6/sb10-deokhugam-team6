package com.codeit.mission.deokhugam.review.entity;

import com.codeit.mission.deokhugam.base.BaseEntity;
import com.codeit.mission.deokhugam.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "review_likes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_review_user_like",
            columnNames = {"review_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_review_likes_liked_at_review_id", columnList = "liked_at, review_id"),
        @Index(name = "idx_review_likes_liked_at_user_id", columnList = "liked_at, user_id")
    }
)
public class ReviewLike extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "review_id", nullable = false)
  private Review review;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "liked_at", nullable = false, updatable = false)
  private Instant likedAt;

  @Builder
  public ReviewLike(Review review, User user, Instant likedAt) {
    this.review = review;
    this.user = user;
    this.likedAt = likedAt;
  }

  // 리뷰 좋아요가 영속화 되는 시점마다 likedAt을 설정한다.
  @PrePersist
  void assignLikedAt() {
    if (likedAt == null) {
      likedAt = Instant.now();
    }
  }
}
