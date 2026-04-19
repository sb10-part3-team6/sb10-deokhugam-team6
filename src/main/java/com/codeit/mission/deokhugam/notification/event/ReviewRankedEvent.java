package com.codeit.mission.deokhugam.notification.event;


import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 리뷰 인기 순위 10위 내 선정 이벤트
 */
@Getter
@AllArgsConstructor
public class ReviewRankedEvent {

    private UUID receiverId; // 알림을 받을 유저의 id
    private UUID reviewId; // 랭킹에 선정된 리뷰의 id

}
