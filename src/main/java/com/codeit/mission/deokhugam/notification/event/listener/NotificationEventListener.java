package com.codeit.mission.deokhugam.notification.event.listener;

import com.codeit.mission.deokhugam.notification.event.CommentRegisteredEvent;
import com.codeit.mission.deokhugam.notification.event.ReviewLikedEvent;
import com.codeit.mission.deokhugam.notification.event.ReviewRankedEvent;
import com.codeit.mission.deokhugam.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handleCommentRegisteredEvent(CommentRegisteredEvent event) {
        notificationService.createByComment(event.getActorId(), event.getReceiverId(),
            event.getReviewId());
    }

    @EventListener
    public void handleReviewLikedEvent(ReviewLikedEvent event) {
        notificationService.createByLike(event.getActorId(), event.getReceiverId(),
            event.getReviewId());
    }

    @EventListener
    public void handleReviewRankedEvent(ReviewRankedEvent event) {
        notificationService.createByReviewRanked(event.getReceiverId(), event.getReviewId());
    }
}
