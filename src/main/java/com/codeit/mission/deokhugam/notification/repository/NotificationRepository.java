package com.codeit.mission.deokhugam.notification.repository;

import com.codeit.mission.deokhugam.notification.entity.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

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
}
