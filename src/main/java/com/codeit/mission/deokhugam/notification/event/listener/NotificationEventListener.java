package com.codeit.mission.deokhugam.notification.event.listener;

import com.codeit.mission.deokhugam.notification.event.CommentRegisteredEvent;
import com.codeit.mission.deokhugam.notification.event.ReviewLikedEvent;
import com.codeit.mission.deokhugam.notification.event.ReviewRankedEvent;
import com.codeit.mission.deokhugam.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationService notificationService;

  // 발행자의 트랜잭션이 성공적으로 완료된 후에 이벤트가 처리되도록 함
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleCommentRegisteredEvent(CommentRegisteredEvent event) {
    notificationService.createByComment(event.getActorId(), event.getReceiverId(),
      event.getReviewId());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleReviewLikedEvent(ReviewLikedEvent event) {
    notificationService.createByLike(event.getActorId(), event.getReceiverId(),
      event.getReviewId());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleReviewRankedEvent(ReviewRankedEvent event) {
    notificationService.createByReviewRanked(event.getReceiverId(), event.getReviewId());
  }
}
