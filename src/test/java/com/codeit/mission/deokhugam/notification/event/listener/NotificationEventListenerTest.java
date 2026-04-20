package com.codeit.mission.deokhugam.notification.event.listener;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.codeit.mission.deokhugam.notification.event.CommentRegisteredEvent;
import com.codeit.mission.deokhugam.notification.event.ReviewLikedEvent;
import com.codeit.mission.deokhugam.notification.event.ReviewRankedEvent;
import com.codeit.mission.deokhugam.notification.service.NotificationService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener notificationEventListener;

    @Test
    @DisplayName("댓글 등록 이벤트 발생 시 알림 생성")
    void handleCommentRegisteredEvent() {
        // given
        UUID actorId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CommentRegisteredEvent event =
            new CommentRegisteredEvent(actorId, receiverId, reviewId);

        // when
        notificationEventListener.handleCommentRegisteredEvent(event);

        // then
        verify(notificationService).createByComment(actorId, receiverId, reviewId);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("리뷰 좋아요 이벤트 발생 시 알림 생성")
    void handleReviewLikedEvent() {
        // given
        UUID actorId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        ReviewLikedEvent event =
            new ReviewLikedEvent(actorId, receiverId, reviewId);

        // when
        notificationEventListener.handleReviewLikedEvent(event);

        // then
        verify(notificationService).createByLike(actorId, receiverId, reviewId);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("리뷰 랭킹 이벤트 발생 시 알림 생성")
    void handleReviewRankedEvent() {
        // given
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        ReviewRankedEvent event =
            new ReviewRankedEvent(receiverId, reviewId);

        // when
        notificationEventListener.handleReviewRankedEvent(event);

        // then
        verify(notificationService).createByReviewRanked(receiverId, reviewId);
        verifyNoMoreInteractions(notificationService);
    }
}
