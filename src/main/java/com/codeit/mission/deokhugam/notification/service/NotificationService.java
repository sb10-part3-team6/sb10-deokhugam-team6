package com.codeit.mission.deokhugam.notification.service;

import com.codeit.mission.deokhugam.notification.dto.CursorPageResponseNotificationDto;
import com.codeit.mission.deokhugam.notification.dto.NotificationDto;
import com.codeit.mission.deokhugam.notification.dto.NotificationRequestQuery;
import com.codeit.mission.deokhugam.notification.entity.Notification;
import com.codeit.mission.deokhugam.notification.mapper.NotificationMapper;
import com.codeit.mission.deokhugam.notification.repository.NotificationRepository;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.exception.ReviewNotFoundException;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.exception.UserNotFoundException;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    private final NotificationMapper notificationMapper;

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

    public CursorPageResponseNotificationDto findByUserId(UUID userId,
        NotificationRequestQuery query) {

        List<NotificationDto> allContent = notificationRepository.findAll().stream()
            .map(notificationMapper::toDto).toList();

        Slice<Notification> slice =
            notificationRepository.findByUserWithCursor(userId, query);

        long totalCount = notificationRepository.countByUserId(userId);

        List<Notification> content = slice.getContent();

        String nextCursor = null;
        Instant nextAfter = null;

        if (!content.isEmpty()) {
            Notification last = content.get(content.size() - 1);
        }

        List<NotificationDto> dtoContent = slice.getContent()
            .stream()
            .map(notificationMapper::toDto)
            .toList();

        return CursorPageResponseNotificationDto.builder()
            .content(allContent)
            .nextCursor(nextCursor)
            .nextAfter(nextAfter)
            .size(content.size())
            .totalElements(totalCount)
            .hasNext(slice.hasNext())
            .build();

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
            .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Review getReview(UUID reviewId) {
        return reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException(reviewId));
    }
}
