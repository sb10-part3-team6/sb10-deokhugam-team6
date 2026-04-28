package com.codeit.mission.deokhugam.dashboard.batch.controller;

import com.codeit.mission.deokhugam.dashboard.batch.DashboardBatchScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@Profile("dev")
@RequestMapping("/api/dashboard/batch-test")
public class BatchTestController {
  private final DashboardBatchScheduler scheduler;

  @PostMapping
  public void execute_batch_scheduler_now(){
    scheduler.runDashboardAggregation();
  }


}
