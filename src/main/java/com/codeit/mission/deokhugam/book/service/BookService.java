package com.codeit.mission.deokhugam.book.service;

import com.codeit.mission.deokhugam.book.dto.BookCreateRequest;
import com.codeit.mission.deokhugam.book.dto.BookDto;
import com.codeit.mission.deokhugam.book.dto.NaverBookDto;
import com.codeit.mission.deokhugam.book.dto.NaverResponseDto;
import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.book.exception.BookNotFoundException;
import com.codeit.mission.deokhugam.book.exception.DuplicatedIsbnException;
import com.codeit.mission.deokhugam.book.exception.InvalidIsbnException;
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

import static com.codeit.mission.deokhugam.error.ErrorCode.BOOK_NOT_FOUND;

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

        //ISBN 유효성 검증
        validateIsbn13(request.isbn());
        if(bookRepository.existsByIsbn(request.isbn())){
            throw new DuplicatedIsbnException(request.isbn());
        }

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
                throw new WrongFileTypeException(contentType == null ? "null" : contentType);
            }

            return bookImageService.upload(image);
        }
        return null;
    }

    //isbn 기반 Naver API 연동 메서드
    public NaverBookDto getBookInfoFromNaverApi(String isbn) {
        //isbn 유효성 검증
        validateIsbn13(isbn);

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

        //응답값이 없으면 예외 처리
        if(response == null || response.items().isEmpty()) {
            throw new BookNotFoundException();
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

    //링크로부터 파일 byte 가져오기
    private byte[] getBytesInLink(String imageUrl){
        return webClient.get()
                .uri(imageUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }

    //유효한지 여부 확인하고 예외 던지는 메서드
    private void validateIsbn13(String isbn){
        if (!isValidIsbn13(isbn)) {
            throw new InvalidIsbnException(isbn);
        }
    }

    //유효성을 실제로 확인하는 메서드
    private boolean isValidIsbn13(String isbn) {
        if (isbn == null) return false;

        // 13자리 숫자인지 확인
        if (!isbn.matches("\\d{13}")) return false;

        int sum = 0;

        for (int i = 0; i < 12; i++) {
            int digit = isbn.charAt(i) - '0';

            // 짝수/홀수 위치에 따라 가중치 적용
            sum += (i % 2 == 0) ? digit : digit * 3;
        }

        // 체크섬 계산
        int checkDigit = (10 - (sum % 10)) % 10;

        // 마지막 자리와 비교
        return checkDigit == (isbn.charAt(12) - '0');
    }


}
