package com.codeit.mission.deokhugam.dashboard.users.service;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.users.dto.CursorPageResponsePowerUserDto;
import com.codeit.mission.deokhugam.dashboard.users.dto.PowerUserDto;
import com.codeit.mission.deokhugam.dashboard.users.repository.PowerUserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PowerUserService {

  // 파워 유저 계산에 필요한 가중치들.
  private static final double REVIEW_SCORE_WEIGHT = 0.5d;
  private static final double LIKE_COUNT_WEIGHT = 0.2d;
  private static final double RECEIVED_REVIEW_LIKE_WEIGHT = 0.3d;

  // 집계된 파워유저를 담아놓는 레포지토리
  private final PowerUserRepository powerUserRepository;
  // 리뷰의 좋아요들을 담아놓는 레포지토리 (누가 좋아요를 눌렀는지 확인하기 위해 만들어놓음)
  //private final ReviewLikeRepository reviewLikeRepository;

  // 집계된 파워유저를 조회하는 메서드
  @Transactional(readOnly = true)
  public CursorPageResponsePowerUserDto getLatestRankings(
      PeriodType periodType, Long cursor, int size) { // 기간, 커서, 사이즈를 파라미터로 받음.
    int pageSize = Math.max(size, 1); // 사이즈가 0이면 1로 강제 고정

    // CursorPageResponse의 content에 삽입할 PowerUserDto의 List를 추출
    List<PowerUserDto> rows =
        powerUserRepository.findLatestRankingDtosByPeriodType(
            periodType, cursor, PageRequest.of(0, pageSize + 1));

    // 추출한 row의 size가 pageSize보다 크다면 다음 페이지가 존재함.
    boolean hasNext = rows.size() > pageSize;
    // 다음 페이지가 존재한다면, rows를 pageSize만큼만 배열에 삽입
    // 다음 페이지가 존재하지 않으면 content = rows
    List<PowerUserDto> content = hasNext ? new ArrayList<>(rows.subList(0, pageSize)) : rows;

    // nextCursor와 nextAfter를 null로 초기화
    String nextCursor = null;
    String nextAfter = null; // 여기서 nextAfter는 Cursor가 같을 때 Tie-breaking을 해주는 역할

    // 다음 페이지가 존재하면서 content가 비어있지 않으면 nextCursor와 nextAfter를 구해옴
    if (hasNext && !content.isEmpty()) {
      PowerUserDto last = content.get(content.size() - 1); // 현재 페이지의 마지막 파워유저를 기준으로
      nextCursor = String.valueOf(last.rank()); // 해당 유저의 rank가 cursor가 되고
      nextAfter = last.createdAt().toString(); // 해당 유저의 createdAt이 nextAfter가 됨.
    }

    // 총 요소의 개수를 구한다.
    long totalElements = powerUserRepository.countLatestRankingsByPeriodType(periodType);

    // 커서 페이지네이션 DTO로 응답을 반환함.
    return new CursorPageResponsePowerUserDto(
        content,
        nextCursor,
        nextAfter,
        pageSize,
        totalElements,
        hasNext);
  }

}
