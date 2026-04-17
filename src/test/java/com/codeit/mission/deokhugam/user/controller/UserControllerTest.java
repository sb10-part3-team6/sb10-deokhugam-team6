package com.codeit.mission.deokhugam.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.mission.deokhugam.error.GlobalExceptionHandler;
import com.codeit.mission.deokhugam.user.dto.UserDto;
import com.codeit.mission.deokhugam.user.dto.UserLoginRequest;
import com.codeit.mission.deokhugam.user.dto.UserRegisterRequest;
import com.codeit.mission.deokhugam.user.exception.EmailDuplicationException;
import com.codeit.mission.deokhugam.user.exception.LoginFailedException;
import com.codeit.mission.deokhugam.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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
          LocalDateTime.now());

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
          LocalDateTime.now());

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
}
