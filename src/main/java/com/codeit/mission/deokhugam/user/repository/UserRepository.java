package com.codeit.mission.deokhugam.user.repository;

import com.codeit.mission.deokhugam.user.entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);
}
