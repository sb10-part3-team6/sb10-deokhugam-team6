package com.codeit.mission.deokhugam.book.service;

import com.codeit.mission.deokhugam.book.dto.BookCreateRequest;
import com.codeit.mission.deokhugam.book.dto.BookDto;
import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.book.exception.WrongFileTypeException;
import com.codeit.mission.deokhugam.book.mapper.BookDtoMapper;
import com.codeit.mission.deokhugam.book.repository.BookRepository;
import com.codeit.mission.deokhugam.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository bookRepository;
    private final BookImageService bookImageService;
    private final BookDtoMapper bookDtoMapper;

    //도서 생성 메서드
    @Transactional
    public BookDto createBook(BookCreateRequest request, MultipartFile image){
        String imagePath = upload(image);

        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .description(request.description())
                .publisher(request.publisher())
                .publishedDate(request.publishedDate())
                .isbn(request.isbn())
                .thumbnailUrl(imagePath)
                .reviewCount(0)
                .rating(0.0)
                .build();

        return bookDtoMapper.toDto(bookRepository.save(book));
    }

    private String upload(MultipartFile image) {
        //파일이 비지 않았고, 컨텐츠 타입이 image라면 파일 업로드 로직 수행
        if (image != null && !image.isEmpty()) {
            String contentType = image.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                throw new WrongFileTypeException(
                        ErrorCode.WRONG_FILE_TYPE,
                        Map.of("contentType", contentType == null ? "null" : contentType)
                );
            }

            return bookImageService.upload(image);
        }
        return null;
    }
}
