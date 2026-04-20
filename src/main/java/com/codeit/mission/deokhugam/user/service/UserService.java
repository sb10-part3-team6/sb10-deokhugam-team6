package com.codeit.mission.deokhugam.user.service;

import com.codeit.mission.deokhugam.user.dto.UserDto;
import com.codeit.mission.deokhugam.user.dto.UserLoginRequest;
import com.codeit.mission.deokhugam.user.dto.UserRegisterRequest;
import com.codeit.mission.deokhugam.user.dto.UserUpdateRequest;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.exception.EmailDuplicationException;
import com.codeit.mission.deokhugam.user.exception.LoginFailedException;
import com.codeit.mission.deokhugam.user.exception.UserNotFoundException;
import com.codeit.mission.deokhugam.user.mapper.UserMapper;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import java.util.UUID;
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
      throw new EmailDuplicationException(request.email());
    }

    User user = userMapper.toEntity(request);
    User savedUser = userRepository.save(user);

    return userMapper.toDto(savedUser);
  }

  public UserDto login(UserLoginRequest request) {
    User user = userRepository.findByEmail(request.email())
        .orElseThrow(LoginFailedException::new);

    // 비밀번호 체크
    if (!user.getPassword().equals(request.password())) {
      throw new LoginFailedException();
    }

    return userMapper.toDto(user);
  }

  public UserDto getUser(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
    return userMapper.toDto(user);
  }

  @Transactional
  public UserDto updateNickname(UUID id, UserUpdateRequest request) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));

    user.updateNickname(request.nickname());
    return userMapper.toDto(user);
  }

  @Transactional
  public void deleteUser(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));

    userRepository.delete(user);
  }
}
