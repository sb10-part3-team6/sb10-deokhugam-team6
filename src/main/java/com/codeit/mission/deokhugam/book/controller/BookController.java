package com.codeit.mission.deokhugam.book.controller;

import com.codeit.mission.deokhugam.book.dto.request.BookCreateRequest;
import com.codeit.mission.deokhugam.book.dto.response.BookDto;
import com.codeit.mission.deokhugam.book.dto.request.BookUpdateRequest;
import com.codeit.mission.deokhugam.book.dto.request.CursorPageRequestDto;
import com.codeit.mission.deokhugam.book.dto.response.CursorPageResponseBookDto;
import com.codeit.mission.deokhugam.book.dto.response.NaverBookDto;
import com.codeit.mission.deokhugam.book.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "도서 관리", description = "도서 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/books")
public class BookController {

  private final BookService bookService;

  @Operation(
      summary = "도서 등록",
      operationId = "create_1",
      description = "새로운 도서를 등록합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "도서 등록 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패, ISBN 형식 오류 등)"),
      @ApiResponse(responseCode = "409", description = "ISBN 중복"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping
  public ResponseEntity<BookDto> createBook(
      @Valid @RequestPart("bookData") BookCreateRequest bookData,
      @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
  ) {
    return ResponseEntity.status(201).body(bookService.createBook(bookData, thumbnailImage));
  }

  @Operation(
      summary = "ISBN으로 도서 정보 조회",
      operationId = "find_with_isbn_1",
      description = "Naver API를 통해 ISBN으로 도서 정보를 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "도서 정보 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 ISBN 형식"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/info")
  public ResponseEntity<NaverBookDto> getBookDataWithIsbnWithNaverApi(
      @RequestParam("isbn") String isbn
  ) {
    return ResponseEntity.ok(bookService.getBookInfoFromNaverApi(isbn));
  }

  @Operation(
      summary = "OCR 기반 ISBN 인식",
      operationId = "detect_by_ocr_1",
      description = "OCR을 통해 ISBN을 인식합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "ISBN 인식 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 이미지 형식 또는 OCR 인식 실패"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping("/isbn/ocr")
  public ResponseEntity<String> ocrIsbnDetect(@RequestPart("image") MultipartFile image) {
    return ResponseEntity.ok(bookService.ocrIsbnDetect(image));
  }

  @Operation(
      summary = "도서 상세 정보 조회",
      operationId = "find_1",
      description = "도서 ID로 상세 정보를 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "도서 정보 조회 성공"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/{bookId}")
  public ResponseEntity<BookDto> findBook(
      @PathVariable UUID bookId
  ) {
    return ResponseEntity.ok(bookService.getBookEntityOrThrow(bookId));
  }

  @Operation(
      summary = "도서 정보 수정",
      operationId = "update_1",
      description = "도서 정보를 수정합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "도서 정보 수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패, ISBN 형식 오류 등)"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
      @ApiResponse(responseCode = "409", description = "ISBN 중복"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PatchMapping("/{bookId}")
  public ResponseEntity<BookDto> updateBook(
      @PathVariable UUID bookId,
      @Valid @RequestPart("bookData") BookUpdateRequest bookUpdateRequest,
      @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
  ) {
    return ResponseEntity.ok(bookService.updateBook(bookId, bookUpdateRequest, thumbnailImage));
  }

  @Operation(
      summary = "도서 논리 삭제",
      operationId = "logical_delete_1",
      description = "도서를 논리적으로 삭제합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "도서 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/{bookId}")
  public ResponseEntity<Void> deleteBook(
      @PathVariable UUID bookId
  ) {
    bookService.deleteBook(bookId);
    return ResponseEntity.status(204).build();
  }

  @Operation(
      summary = "도서 물리 삭제",
      operationId = "hard_delete_1",
      description = "도서를 물리적으로 삭제합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "도서 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/{bookId}/hard")
  public ResponseEntity<Void> hardDeleteBook(
      @PathVariable UUID bookId
  ) {
    bookService.hardDeleteBook(bookId);
    return ResponseEntity.status(204).build();
  }

  @Operation(
      summary = "도서 목록 조회",
      operationId = "find_all_1",
      description = "검색 조건에 맞는 도서 목록을 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "도서 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류 등)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping
  public ResponseEntity<CursorPageResponseBookDto> getBooks(
      @ModelAttribute CursorPageRequestDto request
  ) {
    return ResponseEntity.ok(bookService.findAllBooks(request));
  }
}
