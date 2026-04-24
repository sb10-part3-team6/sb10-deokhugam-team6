package com.codeit.mission.deokhugam.comment.controller;

import com.codeit.mission.deokhugam.comment.dto.request.CommentCreateRequest;
import com.codeit.mission.deokhugam.comment.dto.request.CommentFindAllRequest;
import com.codeit.mission.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.codeit.mission.deokhugam.comment.dto.response.CommentDto;
import com.codeit.mission.deokhugam.comment.dto.response.CursorPageResponseCommentDto;
import com.codeit.mission.deokhugam.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "댓글 관리", description = "댓글 관련 API")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  @Operation(
      summary = "댓글 등록",
      operationId = "create_2",
      description = "새로운 댓글을 등록합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "댓글 등록 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping
  public ResponseEntity<CommentDto> createComment(
      @Valid @RequestBody CommentCreateRequest request) {
    CommentDto commentDto = commentService.createComment(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(commentDto);
  }

  @Operation(
      summary = "댓글 수정",
      operationId = "update_2",
      description = "본인이 작성한 댓글을 수정합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패, 요청자 ID 누락)"),
      @ApiResponse(responseCode = "403", description = "댓글 수정 권한 없음"),
      @ApiResponse(responseCode = "404", description = "댓글 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PatchMapping(value = "/{commentId}")
  public ResponseEntity<CommentDto> updateComment(@PathVariable("commentId") UUID commentId,
      @RequestParam UUID requestUserId,
      @Valid @RequestBody CommentUpdateRequest request) {
    CommentDto commentDto = commentService.updateComment(commentId, requestUserId, request);
    return ResponseEntity.status(HttpStatus.OK).body(commentDto);
  }

  @Operation(
      summary = "댓글 상세 정보 조회",
      operationId = "find_2",
      description = "특정 댓글의 상세 정보를 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "댓글 조회 성공"),
      @ApiResponse(responseCode = "404", description = "댓글 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping(value = "/{commentId}")
  public ResponseEntity<CommentDto> getComment(@PathVariable("commentId") UUID commentId) {
    CommentDto commentDto = commentService.findComment(commentId);
    return ResponseEntity.status(HttpStatus.OK).body(commentDto);
  }

  @Operation(
      summary = "리뷰 댓글 목록 조회",
      operationId = "find_all_2",
      description = "특정 리뷰에 달린 댓글 목록을 시간순으로 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 방향 오류, 페이지네이션 파리미터 오류, 리뷰 ID 누락)"),
      @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping
  public ResponseEntity<CursorPageResponseCommentDto> getComments(
      @Valid @ModelAttribute CommentFindAllRequest request) {
    return ResponseEntity.status(HttpStatus.OK).body(commentService.findAllComments(request));
  }

  @Operation(
      summary = "댓글 논리 삭제",
      operationId = "logical_delete_2",
      description = "본인이 작성한 댓글을 논리적으로 삭제합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)"),
      @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음"),
      @ApiResponse(responseCode = "404", description = "댓글 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping(value = "/{commentId}")
  public ResponseEntity<Void> deleteComment(@PathVariable("commentId") UUID commentId,
      @RequestParam UUID requestUserId) {
    commentService.softDelete(commentId, requestUserId);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "댓글 물리 삭제",
      operationId = "hard_delete_2",
      description = "본인이 작성한 댓글을 물리적으로 삭제합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)"),
      @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음"),
      @ApiResponse(responseCode = "404", description = "댓글 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping(value = "/{commentId}/hard")
  public ResponseEntity<Void> hardDeleteComment(@PathVariable("commentId") UUID commentId,
      @RequestParam UUID requestUserId) {
    commentService.hardDelete(commentId, requestUserId);
    return ResponseEntity.noContent().build();
  }
}
