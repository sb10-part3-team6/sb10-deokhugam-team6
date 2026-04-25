package com.codeit.mission.deokhugam.user.service;

import com.codeit.mission.deokhugam.user.dto.request.UserLoginRequest;
import com.codeit.mission.deokhugam.user.dto.request.UserRegisterRequest;
import com.codeit.mission.deokhugam.user.dto.request.UserUpdateRequest;
import com.codeit.mission.deokhugam.user.dto.response.UserDto;
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
    User user = findUserByEmail(request.email());

    // 비밀번호 체크
    if (!user.getPassword().equals(request.password())) {
      throw new LoginFailedException();
    }

    return userMapper.toDto(user);
  }

  public UserDto getUser(UUID id) {
    return userMapper.toDto(findUserById(id));
  }

  @Transactional
  public UserDto updateNickname(UUID id, UserUpdateRequest request) {
    User user = findUserById(id);

    user.updateNickname(request.nickname());
    return userMapper.toDto(user);
  }

  @Transactional
  public void deleteUser(UUID id) {
    User user = findUserById(id);

    userRepository.delete(user);
  }

  @Transactional
  public void hardDeleteUser(UUID id) {
    if (userRepository.hardDeleteById(id) == 0) {
      throw new UserNotFoundException(id);
    }
  }

  private User findUserById(UUID id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
  }

  private User findUserByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(LoginFailedException::new);
  }
}
