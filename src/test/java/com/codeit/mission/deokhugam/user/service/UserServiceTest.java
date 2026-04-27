package com.codeit.mission.deokhugam.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.codeit.mission.deokhugam.comment.repository.CommentRepository;
import com.codeit.mission.deokhugam.error.ErrorCode;
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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private NotificationRepository notificationRepository;

  @Spy
  private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

  @InjectMocks
  private UserService userService;

  @Nested
  @DisplayName("회원가입")
  class RegisterTest {

    @Test
    @DisplayName("성공: 유저 정보가 정상적으로 저장됨")
    void register_Success() {
      // given
      UserRegisterRequest request = new UserRegisterRequest("test@example.com", "테스터",
          "Password123!");
      given(userRepository.existsByEmail(request.email())).willReturn(false);

      User savedUser = User.builder()
          .email(request.email())
          .nickname(request.nickname())
          .password(request.password())
          .build();
      given(userRepository.save(any(User.class))).willReturn(savedUser);

      // ArgumentCaptor 설정
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

      // when
      UserDto result = userService.register(request);

      // then
      assertThat(result.email()).isEqualTo(request.email());
      assertThat(result.nickname()).isEqualTo(request.nickname());

      // save 호출 시 인자값 검증
      verify(userRepository).save(userCaptor.capture());
      User capturedUser = userCaptor.getValue();
      assertThat(capturedUser.getEmail()).isEqualTo(request.email());
      assertThat(capturedUser.getNickname()).isEqualTo(request.nickname());
      assertThat(capturedUser.getPassword()).isEqualTo(request.password());
    }

    @Test
    @DisplayName("이메일 중복 실패")
    void register_Fail_EmailDuplication() {
      // given
      UserRegisterRequest request = new UserRegisterRequest("duplicate@example.com", "테스터",
          "Password123!");
      given(userRepository.existsByEmail(request.email())).willReturn(true);

      // when & then
      assertThatThrownBy(() -> userService.register(request))
          .isInstanceOf(EmailDuplicationException.class)
          .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_DUPLICATION);
    }
  }

  @Nested
  @DisplayName("로그인")
  class LoginTest {

    @Test
    @DisplayName("성공")
    void login_Success() {
      // given
      UserLoginRequest request = new UserLoginRequest("test@example.com", "Password123!");
      User user = User.builder()
          .email("test@example.com")
          .password("Password123!")
          .nickname("테스터")
          .build();
      given(userRepository.findByEmail(request.email())).willReturn(Optional.of(user));

      // when
      UserDto result = userService.login(request);

      // then
      assertThat(result.email()).isEqualTo(user.getEmail());
      assertThat(result.nickname()).isEqualTo(user.getNickname());
    }

    @Test
    @DisplayName("비밀번호 불일치 실패")
    void login_Fail_InvalidPassword() {
      // given
      UserLoginRequest request = new UserLoginRequest("test@example.com", "WrongPassword!");
      User user = User.builder()
          .email("test@example.com")
          .password("Password123!")
          .build();
      given(userRepository.findByEmail(request.email())).willReturn(Optional.of(user));

      // when & then
      assertThatThrownBy(() -> userService.login(request))
          .isInstanceOf(LoginFailedException.class)
          .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_INPUT_INVALID);
    }

    @Test
    @DisplayName("계정 정보 없음 실패")
    void login_Fail_UserNotFound() {
      // given
      UserLoginRequest request = new UserLoginRequest("nonexistent@example.com", "Password123!");
      given(userRepository.findByEmail(request.email())).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userService.login(request))
          .isInstanceOf(LoginFailedException.class)
          .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_INPUT_INVALID);
    }
  }

  @Nested
  @DisplayName("유저 정보 조회")
  class GetUserTest {

    @Test
    @DisplayName("성공")
    void getUser_Success() {
      // given
      UUID userId = UUID.randomUUID();
      User user = User.builder()
          .email("test@example.com")
          .nickname("테스터")
          .build();
      given(userRepository.findById(userId)).willReturn(Optional.of(user));

      // when
      UserDto result = userService.getUser(userId);

      // then
      assertThat(result.email()).isEqualTo(user.getEmail());
      assertThat(result.nickname()).isEqualTo(user.getNickname());
    }

    @Test
    @DisplayName("실패: 유저 없음")
    void getUser_Fail_NotFound() {
      // given
      UUID userId = UUID.randomUUID();
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userService.getUser(userId))
          .isInstanceOf(UserNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("닉네임 수정")
  class UpdateNicknameTest {

    @Test
    @DisplayName("성공")
    void updateNickname_Success() {
      // given
      UUID userId = UUID.randomUUID();
      User user = User.builder()
          .email("test@example.com")
          .nickname("기존닉네임")
          .build();
      UserUpdateRequest request = new UserUpdateRequest("새닉네임");
      given(userRepository.findById(userId)).willReturn(Optional.of(user));

      // when
      UserDto result = userService.updateNickname(userId, request);

      // then
      assertThat(result.nickname()).isEqualTo("새닉네임");
      assertThat(user.getNickname()).isEqualTo("새닉네임");
    }

    @Test
    @DisplayName("실패: 유저 없음")
    void updateNickname_Fail_NotFound() {
      // given
      UUID userId = UUID.randomUUID();
      UserUpdateRequest request = new UserUpdateRequest("새닉네임");
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userService.updateNickname(userId, request))
          .isInstanceOf(UserNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("회원 탈퇴")
  class DeleteUserTest {

    @Test
    @DisplayName("성공: 유저가 정상적으로 삭제됨")
    void deleteUser_Success() {
      //given
      UUID userId = UUID.randomUUID();
      User user = User.builder()
          .email("test@example.com")
          .nickname("테스터")
          .build();
      given(userRepository.findById(userId)).willReturn(Optional.of(user));

      //when
      userService.deleteUser(userId);

      //then
      verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("실패: 유저가 존재하지 않음")
    void deleteUser_Fail_NotFound() {
      //given
      UUID userId = UUID.randomUUID();
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      //when & then
      assertThatThrownBy(() -> userService.deleteUser(userId))
          .isInstanceOf(UserNotFoundException.class);

      verify(userRepository, never()).delete(any(User.class));
    }
  }

  @Nested
  @DisplayName("회원 물리 삭제")
  class HardDeleteUserTest {

    @Test
    @DisplayName("성공: 물리 삭제가 정상적으로 수행됨")
    void hardDeleteUser_Success() {
      // given
      UUID userId = UUID.randomUUID();
      List<UUID> userIds = Collections.singletonList(userId);
      given(userRepository.existsByDeletedUser(userId)).willReturn(true);
      given(userRepository.hardDeleteById(userId)).willReturn(1);

      // when
      userService.hardDeleteUser(userId);

      // then
      InOrder inOrder = inOrder(reviewRepository, commentRepository, notificationRepository,
          userRepository);

      inOrder.verify(reviewRepository).deleteLikesByReviewUserIds(userIds);
      inOrder.verify(commentRepository).deleteByReviewUserIds(userIds);
      inOrder.verify(notificationRepository).deleteByReviewUserIds(userIds);

      inOrder.verify(reviewRepository).deleteLikesByUserIds(userIds);
      inOrder.verify(commentRepository).deleteByUserIds(userIds);
      inOrder.verify(notificationRepository).deleteByUserIds(userIds);
      inOrder.verify(reviewRepository).deleteByUserIds(userIds);

      inOrder.verify(userRepository).hardDeleteById(userId);
    }

    @Test
    @DisplayName("실패: 삭제 대상 유저가 존재하지 않음 (existsByDeletedUser false)")
    void hardDeleteUser_Fail_NotFound() {
      // given
      UUID userId = UUID.randomUUID();
      given(userRepository.existsByDeletedUser(userId)).willReturn(false);

      // when & then
      assertThatThrownBy(() -> userService.hardDeleteUser(userId))
          .isInstanceOf(UserNotFoundException.class);

      verify(userRepository, never()).hardDeleteById(any(UUID.class));
      verifyNoInteractions(reviewRepository, commentRepository, notificationRepository);
    }

    @Test
    @DisplayName("실패: 물리 삭제 결과가 1이 아님 (IllegalStateException)")
    void hardDeleteUser_Fail_DeletedCountNotOne() {
      // given
      UUID userId = UUID.randomUUID();
      given(userRepository.existsByDeletedUser(userId)).willReturn(true);
      given(userRepository.hardDeleteById(userId)).willReturn(0);

      // when & then
      assertThatThrownBy(() -> userService.hardDeleteUser(userId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("삭제 대상 유저가 존재하지 않거나 이미 삭제되었습니다.");
    }
  }
}
