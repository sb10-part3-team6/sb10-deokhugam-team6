package com.codeit.mission.deokhugam.dashboard.reviews.dto;

import java.util.UUID;

public record ReviewStat(
  UUID reviewId,
  long likeCount,
  long commentCount)
{}
