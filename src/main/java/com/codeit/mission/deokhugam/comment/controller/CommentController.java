package com.codeit.mission.deokhugam.comment.controller;

import com.codeit.mission.deokhugam.comment.dto.request.CommentCreateRequest;
import com.codeit.mission.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.codeit.mission.deokhugam.comment.dto.response.CommentDto;
import com.codeit.mission.deokhugam.comment.repository.CommentRepository;
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
    private final CommentRepository commentRepository;

    @PostMapping
    public ResponseEntity<CommentDto> createComment(@Valid @RequestBody CommentCreateRequest request) {
        CommentDto commentDto = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentDto);
    }

    @PatchMapping(value = "/{commentId}")
    public void updateComment(@PathVariable("commentId") UUID commentId,
                              @RequestParam UUID requestUserId,
                              @Valid @RequestBody CommentUpdateRequest request){
        CommentDto commentDto = commentService.updateComment(commentId, requestUserId, request);
    }

    @GetMapping(value = "/{commentId}")
    public ResponseEntity<CommentDto> getComment(@PathVariable("commentId") UUID commentId) {

    }
}
