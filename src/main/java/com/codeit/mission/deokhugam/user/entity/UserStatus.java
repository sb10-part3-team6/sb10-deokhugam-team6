package com.codeit.mission.deokhugam.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
  ACTIVE("활성"),
  DELETED("탈퇴");

  private final String description;
}
