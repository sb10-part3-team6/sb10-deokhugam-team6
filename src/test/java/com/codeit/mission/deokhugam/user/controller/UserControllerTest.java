package com.codeit.mission.deokhugam.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.mission.deokhugam.error.GlobalExceptionHandler;
import com.codeit.mission.deokhugam.user.dto.request.UserLoginRequest;
import com.codeit.mission.deokhugam.user.dto.request.UserRegisterRequest;
import com.codeit.mission.deokhugam.user.dto.request.UserUpdateRequest;
import com.codeit.mission.deokhugam.user.dto.response.UserDto;
import com.codeit.mission.deokhugam.user.exception.EmailDuplicationException;
import com.codeit.mission.deokhugam.user.exception.LoginFailedException;
import com.codeit.mission.deokhugam.user.exception.UserNotFoundException;
import com.codeit.mission.deokhugam.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @Nested
  @DisplayName("회원가입 API")
  class RegisterTest {

    @Test
    @DisplayName("성공: 201 Created를 반환")
    void register_Success() throws Exception {
      // given
      UserRegisterRequest request = new UserRegisterRequest("test@example.com", "테스터",
          "Password123!");
      UserDto response = new UserDto(UUID.randomUUID(), "test@example.com", "테스터",
          Instant.now());

      given(userService.register(any(UserRegisterRequest.class))).willReturn(response);

      // when & then
      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.email").value("test@example.com"))
          .andExpect(jsonPath("$.nickname").value("테스터"));
    }

    @Test
    @DisplayName("실패: 유효하지 않은 이메일 형식 (400 Bad Request)")
    void register_Fail_InvalidEmail() throws Exception {
      // given
      UserRegisterRequest request = new UserRegisterRequest("invalid-email", "테스터", "Password123!");

      // when & then
      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패: 닉네임 공백 (400 Bad Request)")
    void register_Fail_BlankNickname() throws Exception {
      //given
      UserRegisterRequest request = new UserRegisterRequest("test@example.com", " ",
          "Password123!");

      //when & then
      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패: 비밀번호 정책 미준수 (400 Bad Request)")
    void register_Fail_InvalidPassword() throws Exception {
      // given
      UserRegisterRequest request = new UserRegisterRequest("test@example.com", "테스터", "1234");

      // when & then
      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패: 이메일 중복 (409 Conflict)")
    void register_Fail_DuplicateEmail() throws Exception {
      // given
      UserRegisterRequest request = new UserRegisterRequest("duplicate@example.com", "테스터",
          "Password123!");
      given(userService.register(any(UserRegisterRequest.class)))
          .willThrow(new EmailDuplicationException("duplicate@example.com"));

      // when & then
      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isConflict());
    }
  }

  @Nested
  @DisplayName("로그인 API")
  class LoginTest {

    @Test
    @DisplayName("성공: 200 OK를 반환")
    void login_Success() throws Exception {
      // given
      UserLoginRequest request = new UserLoginRequest("test@example.com", "Password123!");
      UserDto response = new UserDto(UUID.randomUUID(), "test@example.com", "테스터",
          Instant.now());

      given(userService.login(any(UserLoginRequest.class))).willReturn(response);

      // when & then
      mockMvc.perform(post("/api/users/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("실패: 로그인 정보 불일치 (401 Unauthorized)")
    void login_Fail_InvalidCredentials() throws Exception {
      // given
      UserLoginRequest request = new UserLoginRequest("test@example.com", "WrongPassword!");
      given(userService.login(any(UserLoginRequest.class))).willThrow(new LoginFailedException());

      // when & then
      mockMvc.perform(post("/api/users/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("유저 정보 조회 API")
  class GetUserApiTest {

    @Test
    @DisplayName("성공: 200 OK를 반환")
    void getUser_Success() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserDto response = new UserDto(userId, "test@example.com", "테스터", Instant.now());
      given(userService.getUser(userId)).willReturn(response);

      // when & then
      mockMvc.perform(get("/api/users/{userId}", userId))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.email").value("test@example.com"))
          .andExpect(jsonPath("$.nickname").value("테스터"));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 유저 (404 Not Found)")
    void getUser_Fail_NotFound() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      given(userService.getUser(userId)).willThrow(new UserNotFoundException(userId));

      // when & then
      mockMvc.perform(get("/api/users/{userId}", userId))
          .andDo(print())
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }
  }

  @Nested
  @DisplayName("닉네임 수정 API")
  class UpdateNicknameApiTest {

    @Test
    @DisplayName("성공: 200 OK를 반환")
    void updateNickname_Success() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserUpdateRequest request = new UserUpdateRequest("새닉네임");
      UserDto response = new UserDto(userId, "test@example.com", "새닉네임", Instant.now());
      given(userService.updateNickname(any(UUID.class), any(UserUpdateRequest.class))).willReturn(
          response);

      // when & then
      mockMvc.perform(patch("/api/users/{userId}", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.nickname").value("새닉네임"));
    }

    @Test
    @DisplayName("실패: 유효하지 않은 닉네임 (400 Bad Request)")
    void updateNickname_Fail_InvalidInput() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserUpdateRequest request = new UserUpdateRequest("a"); // 2자 미만

      // when & then
      mockMvc.perform(patch("/api/users/{userId}", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 유저 (404 Not Found)")
    void updateNickname_Fail_NotFound() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserUpdateRequest request = new UserUpdateRequest("새닉네임");
      given(userService.updateNickname(any(UUID.class), any(UserUpdateRequest.class)))
          .willThrow(new UserNotFoundException(userId));

      // when & then
      mockMvc.perform(patch("/api/users/{userId}", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }
  }

  @Nested
  @DisplayName("회원 탈퇴 API")
  class DeleteUserApiTest {

    @Test
    @DisplayName("성공: 204 No Content를 반환")
    void deleteUser_Success() throws Exception {
      //given
      UUID userId = UUID.randomUUID();
      doNothing().when(userService).deleteUser(userId);

      //when & then
      mockMvc.perform(delete("/api/users/{userId}", userId))
          .andDo(print())
          .andExpect(status().isNoContent());

      verify(userService).deleteUser(userId);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 유저 (404 Not Found)")
    void deleteUser_Fail_NotFound() throws Exception {
      //given
      UUID userId = UUID.randomUUID();
      doThrow(new UserNotFoundException(userId)).when(userService).deleteUser(userId);

      //when & then
      mockMvc.perform(delete("/api/users/{userId}", userId))
          .andDo(print())
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));

      verify(userService).deleteUser(userId);
    }
  }

  @Nested
  @DisplayName("회원 물리 삭제 API")
  class HardDeleteUserApiTest {

    @Test
    @DisplayName("성공: 204 No Content를 반환")
    void hardDeleteUser_Success() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      doNothing().when(userService).hardDeleteUser(userId);

      // when & then
      mockMvc.perform(delete("/api/users/{userId}/hard", userId))
          .andDo(print())
          .andExpect(status().isNoContent());

      verify(userService).hardDeleteUser(userId);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 유저 (404 Not Found)")
    void hardDeleteUser_Fail_NotFound() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      doThrow(new UserNotFoundException(userId)).when(userService).hardDeleteUser(userId);

      // when & then
      mockMvc.perform(delete("/api/users/{userId}/hard", userId))
          .andDo(print())
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));

      verify(userService).hardDeleteUser(userId);
    }

    @Test
    @DisplayName("실패: 서버 내부 오류 (500 Internal Server Error)")
    void hardDeleteUser_Fail_InternalError() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      doThrow(new IllegalStateException("삭제 대상 유저가 존재하지 않거나 이미 삭제되었습니다."))
          .when(userService).hardDeleteUser(userId);

      // when & then
      mockMvc.perform(delete("/api/users/{userId}/hard", userId))
          .andDo(print())
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));

      verify(userService).hardDeleteUser(userId);
    }
  }
}
