package com.codeit.mission.deokhugam.notification.repository;

import com.codeit.mission.deokhugam.notification.entity.Notification;
import com.codeit.mission.deokhugam.notification.repository.custom.NotificationRepositoryCustom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, UUID>,
  NotificationRepositoryCustom {

  // 사용자와 관련된 모든 알림을 일괄 삭제
  @Modifying
  @Transactional
  @Query(value = "DELETE FROM notifications WHERE user_id IN :userIds", nativeQuery = true)
  void deleteByUserIds(@Param("userIds") List<UUID> userIds);

  // 삭제 대상 유저들이 작성한 리뷰와 관련된 모든 알림을 일괄 삭제 (서브쿼리 활용)
  @Modifying
  @Transactional
  @Query(value = "DELETE FROM notifications WHERE review_id IN (SELECT id FROM reviews WHERE user_id IN :userIds)", nativeQuery = true)
  void deleteByReviewUserIds(@Param("userIds") List<UUID> userIds);

  // 알림 상태를 읽음으로 벌크 업데이트
  // 업데이트 된 레코드 수를 파악하기 위해 리턴 타입을 int로 지정
  @Modifying(clearAutomatically = true)
  @Transactional
  @Query("UPDATE Notification n SET n.confirmed = true WHERE n.user.id = :userId AND n.confirmed = false")
  int updateAllAsConfirmed(@Param("userId") UUID userId);

  // 삭제 대상 리뷰에 관한 알림 일괄 삭제
  @Modifying(clearAutomatically = true)
  @Transactional
  @Query(value = "DELETE FROM Notification notification WHERE notification.review.id IN :reviewIds")
  void deleteByReviewIdIn(@Param("reviewIds") List<UUID> reviewIds);

  // 확인한 알림 중 1주일이 경과된 알림을 일괄적으로 삭제
  @Modifying(clearAutomatically = true)
  @Transactional
  @Query("DELETE FROM Notification n WHERE n.updatedAt < :cutoff AND n.confirmed = true")
  int deleteByConfirmedTrueAndUpdatedAtBefore(Instant cutoff);
}
