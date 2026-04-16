package com.codeit.mission.deokhugam.dashboard.users.service;

import static org.junit.jupiter.api.Assertions.*;

import com.codeit.mission.deokhugam.dashboard.users.repository.PowerUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

class PowerUserServiceTest {

  @MockBean
  PowerUserRepository powerUserRepository;

  @Test
  @DisplayName("레포지토리에 있는 최신 ㅇ ")
  void getLatestRankings() {
  }
}