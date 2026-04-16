package com.codeit.mission.deokhugam.notification.entity;

import com.codeit.mission.deokhugam.base.BaseEntity;
import com.codeit.mission.deokhugam.domain.user.entity.User;
import com.codeit.mission.deokhugam.review.entity.Review;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

  @Column(name = "review_content", nullable = false)
  private String reviewContent;

  @Column(name = "message", nullable = false)
  private String message;

  @Column(name = "confirmed", nullable = false)
  private boolean confirmed = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user; // 알림을 수신한 사용자의 ID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id")
  private Review review; // 좋아요 또는 댓글이 달린 리뷰

  public void updateConfirmed(boolean confirmed) {
    this.confirmed = confirmed;
  }

}
