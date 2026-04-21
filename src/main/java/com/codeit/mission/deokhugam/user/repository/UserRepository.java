package com.codeit.mission.deokhugam.user.repository;

import com.codeit.mission.deokhugam.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);

  // 논리 삭제된 지 일정 시간이 지난 사용자 ID 목록 조회 (SQLRestriction 우회 필요)
  // 대용량 처리를 위해 Pageable을 지원하며, 빈 리스트 에러 방지를 위해 @Nullable 사용
  @Query(value = "SELECT id FROM users WHERE status = 'DELETED' AND updated_at <= :threshold",
         countQuery = "SELECT count(*) FROM users WHERE status = 'DELETED' AND updated_at <= :threshold",
         nativeQuery = true)
  Page<UUID> findDeletedUserIdsOlderThan(@Param("threshold") LocalDateTime threshold, @Nullable Pageable pageable);

  // 사용자 테이블에서 해당 ID들을 영구 삭제 (물리 삭제)
  // TOCTOU 방지를 위해 삭제 시점에 상태와 시간을 다시 한번 검증함
  @Modifying
  @Transactional
  @Query(value = "DELETE FROM users WHERE id IN :ids AND status = 'DELETED' AND updated_at <= :threshold", nativeQuery = true)
  void hardDeleteByIds(@Param("ids") List<UUID> ids, @Param("threshold") LocalDateTime threshold);
}
