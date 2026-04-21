package com.codeit.mission.deokhugam.review.entity;

import com.codeit.mission.deokhugam.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@RequiredArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table( // 추후 schema.sql을 통해 테이블을 인식할 수 있도록 해야 할듯 싶습니다.
    name = "review_likes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_reviews_likes_review_user", columnNames = {"review_id", "user_id"})
    })
public class ReviewLike extends BaseEntity {

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private UUID reviewId;

  public ReviewLike(UUID userId, UUID reviewId){
    this.reviewId = reviewId;
    this.userId = userId;
  }
}
