package com.codeit.mission.deokhugam.user.service;

import com.codeit.mission.deokhugam.error.ErrorCode;
import com.codeit.mission.deokhugam.user.dto.UserDto;
import com.codeit.mission.deokhugam.user.dto.UserLoginRequest;
import com.codeit.mission.deokhugam.user.dto.UserRegisterRequest;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.exception.UserException;
import com.codeit.mission.deokhugam.user.mapper.UserMapper;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import java.util.Map;
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
      throw new UserException(ErrorCode.EMAIL_DUPLICATION, Map.of("email", request.email()));
    }

    User user = userMapper.toEntity(request);
    User savedUser = userRepository.save(user);

    return userMapper.toDto(savedUser);
  }

  public UserDto login(UserLoginRequest request) {
    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new UserException(ErrorCode.LOGIN_INPUT_INVALID));

    // 비밀번호 체크
    if (!user.getPassword().equals(request.password())) {
      throw new UserException(ErrorCode.LOGIN_INPUT_INVALID);
    }

    return userMapper.toDto(user);
  }
}
