package com.codeit.mission.deokhugam.notification.event;


import java.util.List;
import java.util.UUID;
import lombok.Getter;

/**
 * 리뷰 인기 순위 10위 내 선정 이벤트
 */
@Getter
public class ReviewRankedEvent {

  private final List<UUID> reviewIds;

  public ReviewRankedEvent(List<UUID> reviewIds) {
    this.reviewIds = reviewIds;
  }

}
