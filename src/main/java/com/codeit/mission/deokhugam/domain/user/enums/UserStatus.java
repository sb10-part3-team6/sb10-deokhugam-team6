
package com.codeit.mission.deokhugam.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE("활성"),
    DELETED("탈퇴");

    private final String description;
}
