package com.codeit.mission.deokhugam.dashboard.controller;

import com.codeit.mission.deokhugam.dashboard.batch.DashboardBatchScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BatchTestController {
  private final DashboardBatchScheduler dashboardBatchScheduler;

  @PostMapping("/api/dashboard/aggregate")
  public void executeScheduler(){
    dashboardBatchScheduler.runDashboardAggregation();
  }
}
