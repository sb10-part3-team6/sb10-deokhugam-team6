package com.codeit.mission.deokhugam.book.controller;

import com.codeit.mission.deokhugam.book.dto.BookCreateRequest;
import com.codeit.mission.deokhugam.book.dto.BookDto;
import com.codeit.mission.deokhugam.book.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    @PostMapping()
    public ResponseEntity<BookDto> createBook(
            @Valid @RequestPart("bookData") BookCreateRequest bookData,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) {
        return ResponseEntity.status(201).body(bookService.createBook(bookData, thumbnailImage));
    }
}
