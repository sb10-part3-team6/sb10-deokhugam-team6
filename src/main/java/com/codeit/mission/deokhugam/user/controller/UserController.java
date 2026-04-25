package com.codeit.mission.deokhugam.user.controller;

import com.codeit.mission.deokhugam.user.dto.request.UserLoginRequest;
import com.codeit.mission.deokhugam.user.dto.request.UserRegisterRequest;
import com.codeit.mission.deokhugam.user.dto.request.UserUpdateRequest;
import com.codeit.mission.deokhugam.user.dto.response.UserDto;
import com.codeit.mission.deokhugam.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 관리", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @Operation(
      summary = "회원가입",
      operationId = "register_6",
      description = "새로운 사용자를 등록합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "회원가입 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "409", description = "이메일 중복"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping
  public ResponseEntity<UserDto> register(@Valid @RequestBody UserRegisterRequest request) {
    UserDto userDto = userService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
  }

  @Operation(
      summary = "로그인",
      operationId = "login_6",
      description = "사용자 로그인을 처리합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그인 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "401", description = "로그인 실패 (이메일 또는 비밀번호 불일치)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping("/login")
  public ResponseEntity<UserDto> login(@Valid @RequestBody UserLoginRequest request) {
    UserDto userDto = userService.login(request);
    return ResponseEntity.ok(userDto);
  }

  @Operation(
      summary = "사용자 정보 조회",
      operationId = "find_6",
      description = "사용자 ID로 상세 정보를 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> getUser(@PathVariable UUID userId) {
    UserDto userDto = userService.getUser(userId);
    return ResponseEntity.ok(userDto);
  }

  @Operation(
      summary = "사용자 정보 수정",
      operationId = "update_6",
      description = "사용자의 닉네임을 수정합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "403", description = "사용자 정보 수정 권한 없음"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PatchMapping("/{userId}")
  public ResponseEntity<UserDto> updateNickname(
      @PathVariable UUID userId,
      @Valid @RequestBody UserUpdateRequest request) {
    UserDto userDto = userService.updateNickname(userId, request);
    return ResponseEntity.ok(userDto);
  }

  @Operation(
      summary = "사용자 논리 삭제",
      operationId = "logical_delete_6",
      description = "사용자를 논리적으로 삭제합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "사용자 삭제 성공"),
      @ApiResponse(responseCode = "403", description = "사용자 삭제 권한 없음"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
    userService.deleteUser(userId);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "사용자 물리 삭제",
      operationId = "permanentDeleteUser",
      description = "사용자를 물리적으로 삭제합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "사용자 삭제 성공"),
      @ApiResponse(responseCode = "403", description = "사용자 삭제 권한 없음"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/{userId}/hard")
  public ResponseEntity<Void> hardDeleteUser(@PathVariable UUID userId) {
    userService.hardDeleteUser(userId);
    return ResponseEntity.noContent().build();
  }
}
