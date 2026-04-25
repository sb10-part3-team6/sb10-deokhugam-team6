package com.codeit.mission.deokhugam.user.service;

import com.codeit.mission.deokhugam.comment.repository.CommentRepository;
import com.codeit.mission.deokhugam.notification.repository.NotificationRepository;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;
  private final ReviewRepository reviewRepository;
  private final CommentRepository commentRepository;
  private final NotificationRepository notificationRepository;
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
    // 0. 대상 유저가 실제로 존재하고 'DELETED' 상태인지 확인 (외래 키 제약 위반 방지 및 안전한 삭제를 위함)
    if (!userRepository.existsByDeletedUser(id)) {
      throw new UserNotFoundException(id);
    }

    List<UUID> userIds = Collections.singletonList(id);

    // 1. 삭제 대상 유저가 작성한 리뷰에 달린 연관 데이터 먼저 삭제 (자식의 자식)
    reviewRepository.deleteLikesByReviewUserIds(userIds);
    commentRepository.deleteByReviewUserIds(userIds);
    notificationRepository.deleteByReviewUserIds(userIds);

    // 2. 유저 본인의 활동 데이터 삭제
    reviewRepository.deleteLikesByUserIds(userIds);
    commentRepository.deleteByUserIds(userIds);
    notificationRepository.deleteByUserIds(userIds);
    reviewRepository.deleteByUserIds(userIds);

    // 3. 최종 유저 본인 삭제 (이미 상태 체크를 했으므로 무조건 1이어야 함)
    userRepository.hardDeleteById(id);
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
