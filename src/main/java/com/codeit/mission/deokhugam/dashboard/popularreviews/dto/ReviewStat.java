package com.codeit.mission.deokhugam.dashboard.popularreviews.dto;

import java.util.UUID;

public record ReviewStat(
  UUID reviewId,
  long likeCount,
  long commentCount)
{}
