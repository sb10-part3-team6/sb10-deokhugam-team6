package com.codeit.mission.deokhugam.dashboard.users.exception;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.util.Map;

public class PowerUserAggregationFailed extends DeokhugamException {


  public PowerUserAggregationFailed(PeriodType periodType) {
    super(ErrorCode.POWER_AGGREGATION_BATCH_JOB_FAILED, Map.of("PeriodType", periodType ));
  }
}
