package com.codeit.mission.deokhugam.book.service;

import com.codeit.mission.deokhugam.book.dto.BookCreateRequest;
import com.codeit.mission.deokhugam.book.dto.BookDto;
import com.codeit.mission.deokhugam.book.dto.NaverBookDto;
import com.codeit.mission.deokhugam.book.dto.NaverResponseDto;
import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.book.exception.WrongFileTypeException;
import com.codeit.mission.deokhugam.book.mapper.BookDtoMapper;
import com.codeit.mission.deokhugam.book.repository.BookRepository;
import com.codeit.mission.deokhugam.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository bookRepository;
    private final BookImageService bookImageService;
    private final BookDtoMapper bookDtoMapper;
    private final WebClient webClient;

    private final String NAVER_BOOK_API_URL = "https://openapi.naver.com/v1/search/book_adv";

    @Value("${naverapi.client-id}")
    private String NAVER_CLIENT_ID;
    @Value("${naverapi.client-secret}")
    private String NAVER_CLIENT_SECRET;

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

    //isbn 기반 Naver API 연동 메서드
    public NaverBookDto getBookInfoFromNaverApi(String isbn) {
        NaverResponseDto response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(NAVER_BOOK_API_URL)
                        .queryParam("d_isbn", isbn)
                        .build()
                )
                .header("X-Naver-Client-Id", NAVER_CLIENT_ID)
                .header("X-Naver-Client-Secret", NAVER_CLIENT_SECRET)
                .retrieve()
                .bodyToMono(NaverResponseDto.class)
                .block();

        if(response == null || response.items().isEmpty()) {
            throw new RuntimeException("Book not found");
        }

        // 응답받은 날짜값을 LocalDate로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate pubdate = LocalDate.parse(response.items().get(0).pubdate(), formatter);

        return NaverBookDto.builder()
                .title(response.items().get(0).title())
                .author(response.items().get(0).author())
                .description(response.items().get(0).description())
                .publisher(response.items().get(0).publisher())
                .publishedDate(pubdate)
                .isbn(response.items().get(0).isbn())
                .thumbnailImage(getBytesInLink(response.items().get(0).image()))
                .build();
    }

    private byte[] getBytesInLink(String imageUrl){
        return webClient.get()
                .uri(imageUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }
}
