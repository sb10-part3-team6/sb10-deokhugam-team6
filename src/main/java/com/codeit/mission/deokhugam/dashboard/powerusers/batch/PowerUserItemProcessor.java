package com.codeit.mission.deokhugam.dashboard.powerusers.batch;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.exceptions.InvalidJobParameterException;
import com.codeit.mission.deokhugam.dashboard.powerusers.dto.UserStat;
import com.codeit.mission.deokhugam.dashboard.powerusers.entity.PowerUser;
import com.codeit.mission.deokhugam.dashboard.powerusers.service.PowerUserAggregateService;
import com.codeit.mission.deokhugam.user.entity.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@StepScope
public class PowerUserItemProcessor implements ItemProcessor<User, PowerUser> {
  private final PowerUserAggregateService powerUserAggregateService;

  private PeriodType periodType;
  private LocalDateTime aggregatedAt;
  private UUID snapshotId;
  private Map<UUID, UserStat> statsByUserId = Map.of();

  // Step ?쒖옉 ??留ㅺ컻蹂?섎뱾??珥덇린?뷀븳??
  // ?꾩옱 ?ㅽ뻾 媛앹껜??context瑜?二쇱엯諛쏆븘 蹂?섎? 珥덇린?뷀븳??
  @BeforeStep
  void beforeStep(StepExecution stepExecution){
    String periodTypeStr = stepExecution.getJobExecution().getJobParameters().getString("periodType");
    String aggregatedAtStr = stepExecution.getJobExecution().getJobParameters().getString("aggregatedAt");
    String snapshotIdStr = stepExecution.getJobExecution().getExecutionContext().getString("snapshotId");

    Map<String, Object> details = new LinkedHashMap<>();
    if (periodTypeStr == null || periodTypeStr.isBlank()) {
      details.put("periodType", periodTypeStr != null ? periodTypeStr : "null");
    }
    if (aggregatedAtStr == null || aggregatedAtStr.isBlank()) {
      details.put("aggregatedAt", aggregatedAtStr != null ? aggregatedAtStr : "null");
    }
    if (snapshotIdStr == null || snapshotIdStr.isBlank()) {
      details.put("snapshotId", snapshotIdStr != null ? snapshotIdStr : "null");
    }
    if (!details.isEmpty()) {
      throw new InvalidJobParameterException(details);
    }

    try {
      this.periodType = PeriodType.valueOf(periodTypeStr);
    } catch (IllegalArgumentException e) {
      throw new InvalidJobParameterException(Map.of("periodType", periodTypeStr));
    }

    try {
      this.aggregatedAt = LocalDateTime.parse(aggregatedAtStr);
    } catch (DateTimeParseException e) {
      throw new InvalidJobParameterException(Map.of("aggregatedAt", aggregatedAtStr));
    }

    try {
      this.snapshotId = UUID.fromString(snapshotIdStr);
    } catch (IllegalArgumentException e) {
      throw new InvalidJobParameterException(Map.of("snapshotId", snapshotIdStr));
    }

    this.statsByUserId = powerUserAggregateService.loadUserStats(periodType, aggregatedAt); // Aggregate ?쒕퉬?ㅼ뿉???좎? ?ㅽ꺈??濡쒕뱶?섎뒗 硫붿꽌???몄텧
  }

  @Override
  public @Nullable PowerUser process(@NonNull User item) throws Exception {
    // User id 蹂?UserStat???댁쟾 itemReader??諛섑솚??User濡쒕???媛?몄삩??
    UserStat stat =
        statsByUserId.getOrDefault(item.getId(), powerUserAggregateService.emptyStat(item.getId()));
    return powerUserAggregateService.toPowerUser(item, stat, periodType, aggregatedAt, snapshotId);
  }
}
