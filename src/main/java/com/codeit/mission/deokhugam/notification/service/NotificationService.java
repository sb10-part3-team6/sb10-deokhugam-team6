package com.codeit.mission.deokhugam.notification.service;

import com.codeit.mission.deokhugam.notification.entity.Notification;
import com.codeit.mission.deokhugam.notification.repository.NotificationRepository;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public void createByLike(UUID senderId, UUID receiverId, UUID reviewId) {
        User sender = getUser(senderId);
        User receiver = getUser(receiverId);
        Review review = getReview(reviewId);

        notificationRepository.save(
            createNotification(receiver, review,
                "[" + sender.getNickname() + "]님이 나의 리뷰를 좋아합니다.")
        );
    }

    public void createByComment(UUID senderId, UUID receiverId, UUID reviewId) {
        User sender = getUser(senderId);
        User receiver = getUser(receiverId);
        Review review = getReview(reviewId);

        notificationRepository.save(
            createNotification(receiver, review,
                "[" + sender.getNickname() + "]님이 나의 리뷰에 댓글을 남겼습니다.")
        );
    }

    public void createByReviewRanked(UUID userId, UUID reviewId) {
        User user = getUser(userId);
        Review review = getReview(reviewId);

        notificationRepository.save(
            // fixme: 인기 리뷰 알림 메시지 예시를 확인할 수 없어서 임시로 작성
            createNotification(user, review, "나의 리뷰가 인기 리뷰로 등록되었습니다.")
        );
    }

    private Notification createNotification(
        User receiver,
        Review review,
        String message
    ) {
        return Notification.builder()
            .reviewContent(review.getContent())
            .message(message)
            .confirmed(false)
            .user(receiver)
            .review(review)
            .build();
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(RuntimeException::new); // fixme: UserNotFoundException으로 수정
    }

    private Review getReview(UUID reviewId) {
        return reviewRepository.findById(reviewId)
            .orElseThrow(RuntimeException::new); // fixme: ReviewNotFoundException으로 수정
    }
}
