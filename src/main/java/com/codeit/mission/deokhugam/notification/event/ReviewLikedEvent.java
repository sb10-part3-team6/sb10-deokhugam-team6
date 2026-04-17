package com.codeit.mission.deokhugam.notification.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 리뷰 좋아요 이벤트
 */
@Getter
@AllArgsConstructor
public class ReviewLikedEvent {

    private UUID actorId; // 좋아요를 누른 유저 id
    private UUID receiverId; // 알림을 받을 유저의 id
    private UUID reviewId; // 좋아요가 달린 리뷰의 id


}
