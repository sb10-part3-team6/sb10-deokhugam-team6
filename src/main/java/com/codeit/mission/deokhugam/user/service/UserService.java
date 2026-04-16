package com.codeit.mission.deokhugam.user.service;

import com.codeit.mission.deokhugam.user.dto.UserDto;
import com.codeit.mission.deokhugam.user.dto.UserRegisterRequest;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.mapper.UserMapper;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Transactional
  public UserDto register(UserRegisterRequest request) {
    // 이메일 중복 체크
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalStateException("이미 사용 중인 이메일입니다.");
    }

    // 닉네임 중복 체크
    if (userRepository.existsByNickname(request.nickname())) {
      throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
    }

    User user = userMapper.toEntity(request);
    User savedUser = userRepository.save(user);

    return userMapper.toDto(savedUser);
  }
}
