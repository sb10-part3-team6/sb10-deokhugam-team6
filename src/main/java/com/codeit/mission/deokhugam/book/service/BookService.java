package com.codeit.mission.deokhugam.book.service;

import com.codeit.mission.deokhugam.book.dto.BookCreateRequest;
import com.codeit.mission.deokhugam.book.dto.BookDto;
import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.book.mapper.BookDtoMapper;
import com.codeit.mission.deokhugam.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class BookService {
    private final BookRepository bookRepository;
    private final BookImageService bookImageService;
    private final BookDtoMapper bookDtoMapper;

    public BookDto createBook(BookCreateRequest request, MultipartFile image){
        String imagePath = null;

        if(image != null){
            if(image.getSize() > 0){
                imagePath = bookImageService.upload(image);
            }
        }

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
}
