package com.codeit.mission.deokhugam.comment.entity;

import com.codeit.mission.deokhugam.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "comments")
public class Comment extends BaseEntity {

    // 리뷰 정보
    @Column(name = "review_id", nullable = false)
    private UUID reviewId;

    // 사용자 정보
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // 댓글 내용
    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Builder
    public Comment (UUID reviewId, UUID userId, String content) {
        super();
        this.reviewId = reviewId;
        this.userId = userId;
        this.content = content;
    }

    // 댓글 수정
    public void updateContent(String content) {
        this.content = content;
    }
}
