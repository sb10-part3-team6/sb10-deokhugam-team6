package com.codeit.mission.deokhugam.comment.controller;

import com.codeit.mission.deokhugam.comment.dto.request.CommentCreateRequest;
import com.codeit.mission.deokhugam.comment.dto.request.CommentFindAllRequest;
import com.codeit.mission.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.codeit.mission.deokhugam.comment.dto.response.CommentDto;
import com.codeit.mission.deokhugam.comment.dto.response.CursorPageResponseCommentDto;
import com.codeit.mission.deokhugam.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
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

    // 댓글 생성
    @Operation(summary = "")
    @PostMapping
    public ResponseEntity<CommentDto> createComment(@Valid @RequestBody CommentCreateRequest request) {
        CommentDto commentDto = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentDto);
    }

    // 댓글 수정
    @PatchMapping(value = "/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable("commentId") UUID commentId,
                              @RequestParam UUID requestUserId,
                              @Valid @RequestBody CommentUpdateRequest request){
        CommentDto commentDto = commentService.updateComment(commentId, requestUserId, request);
        return ResponseEntity.status(HttpStatus.OK).body(commentDto);
    }

    // 댓글 상세 조회
    @GetMapping(value = "/{commentId}")
    public ResponseEntity<CommentDto> getComment(@PathVariable("commentId") UUID commentId) {
        CommentDto commentDto = commentService.findComment(commentId);
        return ResponseEntity.status(HttpStatus.OK).body(commentDto);
    }

    // 댓글 목록 조회
    @GetMapping
    public ResponseEntity<CursorPageResponseCommentDto> getComments(@Valid @ModelAttribute CommentFindAllRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(commentService.findAllComments(request));
    }

    // 댓글 논리 삭제
    @DeleteMapping(value = "/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") UUID commentId, @RequestParam UUID requestUserId) {
        commentService.softDelete(commentId, requestUserId);
        return ResponseEntity.noContent().build();
    }

    // 댓글 물리 삭제
    @DeleteMapping(value = "/{commentId}/hard")
    public ResponseEntity<Void> hardDeleteComment(@PathVariable("commentId") UUID commentId, @RequestParam UUID requestUserId) {
        commentService.hardDelete(commentId, requestUserId);
        return ResponseEntity.noContent().build();
    }
}
