package com.codeit.mission.deokhugam.user.repository;

import com.codeit.mission.deokhugam.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);

  // 논리 삭제된 지 일정 시간이 지난 사용자 ID 목록 조회 (SQLRestriction 우회 필요)
  @Query(value = "SELECT id FROM users WHERE status = 'DELETED' AND updated_at <= :threshold", nativeQuery = true)
  List<UUID> findDeletedUserIdsOlderThan(@Param("threshold") LocalDateTime threshold,
      Pageable pageable);

  // 사용자 테이블에서 해당 ID들을 영구 삭제 (물리 삭제)
  @Modifying
  @Query(value = "DELETE FROM users WHERE id IN :ids", nativeQuery = true)
  void hardDeleteByIds(@Param("ids") List<UUID> ids);
}
