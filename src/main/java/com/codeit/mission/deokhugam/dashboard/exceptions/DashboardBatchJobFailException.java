package com.codeit.mission.deokhugam.dashboard.exceptions;

import com.codeit.mission.deokhugam.dashboard.DomainType;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.util.Map;

public class DashboardBatchJobFailException extends DeokhugamException {

  public DashboardBatchJobFailException(DomainType domainType, PeriodType periodType) {

    super(ErrorCode.JOB_FAILED, Map.of(("DomainType: " + domainType),("PeriodType: " + periodType)));
  }
}
