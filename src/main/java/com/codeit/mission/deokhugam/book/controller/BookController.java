package com.codeit.mission.deokhugam.book.controller;

import com.codeit.mission.deokhugam.book.dto.BookCreateRequest;
import com.codeit.mission.deokhugam.book.dto.BookDto;
import com.codeit.mission.deokhugam.book.dto.NaverBookDto;
import com.codeit.mission.deokhugam.book.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    @PostMapping
    public ResponseEntity<BookDto> createBook(
            @Valid @RequestPart("bookData") BookCreateRequest bookData,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) {
        return ResponseEntity.status(201).body(bookService.createBook(bookData, thumbnailImage));
    }

    @GetMapping("/info")
    public ResponseEntity<NaverBookDto> getBookDataWithIsbnWithNaverApi(
            @RequestParam("isbn") String isbn
    ){
        return ResponseEntity.ok(bookService.getBookInfoFromNaverApi(isbn));
    }

    @PostMapping("/isbn/ocr")
    public ResponseEntity<String> ocrIsbnDetect(@RequestPart("image") MultipartFile image){
        return ResponseEntity.ok(bookService.ocrIsbnDetect(image));
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookDto> findBook(
            @PathVariable("bookId") UUID bookId
    ){
        return ResponseEntity.ok(bookService.findBook(bookId));
    }
}
