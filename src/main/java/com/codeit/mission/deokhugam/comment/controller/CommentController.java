package com.codeit.mission.deokhugam.comment.controller;

import com.codeit.mission.deokhugam.comment.dto.request.CommentCreateRequest;
import com.codeit.mission.deokhugam.comment.dto.response.CommentDto;
import com.codeit.mission.deokhugam.comment.repository.CommentRepository;
import com.codeit.mission.deokhugam.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
