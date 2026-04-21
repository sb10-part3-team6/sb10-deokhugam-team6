package com.codeit.mission.deokhugam.comment.controller;

import com.codeit.mission.deokhugam.comment.dto.request.CommentCreateRequest;
import com.codeit.mission.deokhugam.comment.dto.request.CommentFindAllRequest;
import com.codeit.mission.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.codeit.mission.deokhugam.comment.dto.response.CommentDto;
import com.codeit.mission.deokhugam.comment.dto.response.CursorPageResponseCommentDto;
import com.codeit.mission.deokhugam.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    // 댓글 생성
    @PostMapping
    public ResponseEntity<CommentDto> createComment(@Valid @ModelAttribute CommentCreateRequest request) {
        CommentDto commentDto = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentDto);
    }

    // 댓글 수정
    @PatchMapping(value = "/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable("commentId") UUID commentId,
                              @RequestParam UUID requestUserId,
                              @Valid @ModelAttribute CommentUpdateRequest request){
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
    public CursorPageResponseCommentDto getComments(@Valid @ModelAttribute CommentFindAllRequest request) {
        return commentService.findAll(request);
    }
}
