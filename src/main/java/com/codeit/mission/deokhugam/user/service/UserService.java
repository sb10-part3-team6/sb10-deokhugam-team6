package com.codeit.mission.deokhugam.user.service;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import com.codeit.mission.deokhugam.user.dto.UserDto;
import com.codeit.mission.deokhugam.user.dto.UserLoginRequest;
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
      throw new DeokhugamException(ErrorCode.DUPLICATE_EMAIL);
    }

    // 닉네임 중복 체크
    if (userRepository.existsByNickname(request.nickname())) {
      throw new DeokhugamException(ErrorCode.DUPLICATE_NICKNAME);
    }

    User user = userMapper.toEntity(request);
    User savedUser = userRepository.save(user);

    return userMapper.toDto(savedUser);
  }

  public UserDto login(UserLoginRequest request) {
    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new DeokhugamException(ErrorCode.INVALID_PASSWORD));

    // 비밀번호 체크
    if (!user.getPassword().equals(request.password())) {
      throw new DeokhugamException(ErrorCode.INVALID_PASSWORD);
    }

    return userMapper.toDto(user);
  }
}
