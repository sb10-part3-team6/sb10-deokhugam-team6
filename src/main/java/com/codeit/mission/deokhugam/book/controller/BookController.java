package com.codeit.mission.deokhugam.book.controller;

import com.codeit.mission.deokhugam.book.dto.BookCreateRequest;
import com.codeit.mission.deokhugam.book.dto.BookDto;
import com.codeit.mission.deokhugam.book.dto.BookUpdateRequest;
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
            @PathVariable UUID bookId
    ){
        return ResponseEntity.ok(bookService.findBook(bookId));
    }

    @PatchMapping("/{bookId}")
    public ResponseEntity<BookDto> updateBook(
            @PathVariable UUID bookId,
            @Valid @RequestPart BookUpdateRequest bookUpdateRequest,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ){
        return ResponseEntity.ok(bookService.updateBook(bookId, bookUpdateRequest, thumbnailImage));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable UUID bookId
    ){
        bookService.deleteBook(bookId);
        return ResponseEntity.status(204).build();
    }

    @DeleteMapping("/{bookId}/hard")
    public ResponseEntity<Void> hardDeleteBook(
            @PathVariable UUID bookId
    ){
        bookService.hardDeleteBook(bookId);
        return ResponseEntity.status(204).build();
    }
}
