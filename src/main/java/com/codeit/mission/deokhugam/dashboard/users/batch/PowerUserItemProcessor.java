package com.codeit.mission.deokhugam.dashboard.users.batch;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.users.entity.PowerUser;
import com.codeit.mission.deokhugam.dashboard.users.service.PowerUserAggregateService;
import com.codeit.mission.deokhugam.user.entity.User;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@StepScope
public class PowerUserItemProcessor implements ItemProcessor<User, PowerUser> {
  private final PowerUserAggregateService powerUserAggregateService;

  @Value("#{jobParameters['periodType']}")
  private String periodTypeValue;
  @Value("#{jobParameters['aggregatedAt']}")
  private String aggregatedAtValue;

  @Override
  public @Nullable PowerUser process(@NonNull User item) throws Exception {
    PeriodType periodType = PeriodType.valueOf(periodTypeValue);
    LocalDateTime aggregatedAt = LocalDateTime.parse(aggregatedAtValue);

    return powerUserAggregateService.toPowerUser(item, periodType, aggregatedAt);
  }
}
