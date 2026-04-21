package com.codeit.mission.deokhugam.review.entity;

import com.codeit.mission.deokhugam.base.BaseEntity;
import com.codeit.mission.deokhugam.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"review", "user"}, callSuper = false)
@Table(
    name = "review_likes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_review_user_like",
            columnNames = {"review_id", "user_id"})
    })
public class ReviewLike extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "review_id", nullable = false)
  private Review review;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "liked_at", nullable = false, updatable = false)
  private LocalDateTime likedAt;

  @Builder
  public ReviewLike(Review review, User user, LocalDateTime likedAt) {
    this.review = review;
    this.user = user;
    this.likedAt = likedAt;
  }

  // 리뷰 좋아요가 영속화 되는 시점마다 likedAt을 설정한다.
  @PrePersist
  void assignLikedAt() {
    if (likedAt == null) {
      likedAt = LocalDateTime.now();
    }
  }
}
