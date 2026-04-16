package com.codeit.mission.deokhugam.notification.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 리뷰 댓글 등록 이벤트
 */
@Getter
@AllArgsConstructor
public class CommentRegisteredEvent {

    private UUID actorId; // 댓글을 작성한 유저의 id
    private UUID receiverId; // 알림을 받을 유저의 id
    private UUID reviewId; // 랭킹에 선정된 리뷰의 id

}
