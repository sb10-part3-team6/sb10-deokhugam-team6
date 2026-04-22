package com.codeit.mission.deokhugam.notification.service;

import com.codeit.mission.deokhugam.notification.dto.CursorPageResponseNotificationDto;
import com.codeit.mission.deokhugam.notification.dto.NotificationDto;
import com.codeit.mission.deokhugam.notification.dto.NotificationRequestQuery;
import com.codeit.mission.deokhugam.notification.dto.NotificationUpdateRequest;
import com.codeit.mission.deokhugam.notification.entity.Notification;
import com.codeit.mission.deokhugam.notification.exception.NotificationNotFoundException;
import com.codeit.mission.deokhugam.notification.exception.NotificationNotOwnedException;
import com.codeit.mission.deokhugam.notification.mapper.NotificationMapper;
import com.codeit.mission.deokhugam.notification.repository.NotificationRepository;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.exception.ReviewNotFoundException;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.exception.UserNotFoundException;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import java.time.Instant;
import java.time.ZoneOffset;
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
    User sender = getUserEntityOrThrow(senderId);
    User receiver = getUserEntityOrThrow(receiverId);
    Review review = getReviewOrThrow(reviewId);

    notificationRepository.save(
      createNotification(receiver, review,
        "[" + sender.getNickname() + "]님이 나의 리뷰를 좋아합니다.")
    );
  }

  public void createByComment(UUID senderId, UUID receiverId, UUID reviewId) {
    User sender = getUserEntityOrThrow(senderId);
    User receiver = getUserEntityOrThrow(receiverId);
    Review review = getReviewOrThrow(reviewId);

    notificationRepository.save(
      createNotification(receiver, review,
        "[" + sender.getNickname() + "]님이 나의 리뷰에 댓글을 남겼습니다.")
    );
  }

  public void createByReviewRanked(UUID userId, UUID reviewId) {
    User user = getUserEntityOrThrow(userId);
    Review review = getReviewOrThrow(reviewId);

    notificationRepository.save(
      // fixme: 인기 리뷰 알림 메시지 예시를 확인할 수 없어서 임시로 작성
      createNotification(user, review, "나의 리뷰가 인기 리뷰로 등록되었습니다.")
    );
  }

  @Transactional(readOnly = true)
  public CursorPageResponseNotificationDto findByUserId(UUID userId,
    NotificationRequestQuery query) {

    Slice<Notification> slice =
      notificationRepository.findByUserWithCursor(userId, query);

    long totalCount = notificationRepository.countByUserId(userId);

    List<Notification> content = slice.getContent();

    String nextCursor = null;
    Instant nextAfter = null;

    if (!content.isEmpty()) {
      Notification last = content.get(content.size() - 1);

      if (slice.hasNext()) {
        nextCursor = last.getCreatedAt().toInstant(ZoneOffset.UTC).toString();
        nextAfter = last.getCreatedAt().toInstant(ZoneOffset.UTC);
      }

    }

    List<NotificationDto> dtoContent = slice.getContent()
      .stream()
      .map(notificationMapper::toDto)
      .toList();

    return CursorPageResponseNotificationDto.builder()
      .content(dtoContent)
      .nextCursor(nextCursor)
      .nextAfter(nextAfter)
      .size(content.size())
      .totalElements(totalCount)
      .hasNext(slice.hasNext())
      .build();

  }

  public NotificationDto updateById(UUID notificationId, UUID requestUserId,
    NotificationUpdateRequest requestDto) {

    Notification notification = getNotificationOrThrow(notificationId);

    validateNotificationOwner(notification, requestUserId);

    notification.updateConfirmed(requestDto.confirmed());

    return notificationMapper.toDto(notification);
  }

  public void updateByUserId(UUID userId) {
    validateUserExists(userId);
    notificationRepository.updateAllAsConfirmed(userId);
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

  // 요청자의 id와 알림을 받은 사람의 id를 대조
  private void validateNotificationOwner(Notification notification, UUID requestUserId) {
    if (!requestUserId.equals(notification.getUser().getId())) {
      throw new NotificationNotOwnedException();
    }
  }

  // 실제로 존재하는 유저 id인지 확인
  private void validateUserExists(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw new UserNotFoundException(userId);
    }
  }

  private User getUserEntityOrThrow(UUID userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> new UserNotFoundException(userId));
  }

  private Review getReviewOrThrow(UUID reviewId) {
    return reviewRepository.findById(reviewId)
      .orElseThrow(() -> new ReviewNotFoundException(reviewId));
  }

  private Notification getNotificationOrThrow(UUID notificationId) {
    return notificationRepository.findById(notificationId)
      .orElseThrow(() -> new NotificationNotFoundException(notificationId));
  }
}
