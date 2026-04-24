package com.codeit.mission.deokhugam.dashboard.powerusers.dto;

import java.util.UUID;

// 유저 인기 점수를 계산하기 위한 스탯들을 모아놓은 레코드
public record UserStat(
    UUID userId,
    double reviewScoreSum,
    long likeCount,
    long commentCount,
    double score
) {

}
