package com.codeit.mission.deokhugam.user.controller;

import com.codeit.mission.deokhugam.user.dto.UserDto;
import com.codeit.mission.deokhugam.user.dto.UserRegisterRequest;
import com.codeit.mission.deokhugam.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserDto> register(@Valid @RequestBody UserRegisterRequest request) {
    UserDto userDto = userService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
  }
}
