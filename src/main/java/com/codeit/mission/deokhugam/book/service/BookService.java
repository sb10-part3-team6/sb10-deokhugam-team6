package com.codeit.mission.deokhugam.book.service;

import com.codeit.mission.deokhugam.book.dto.*;
import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.book.entity.BookStatus;
import com.codeit.mission.deokhugam.book.event.BookDeletedEvent;
import com.codeit.mission.deokhugam.book.exception.*;
import com.codeit.mission.deokhugam.book.mapper.BookDtoMapper;
import com.codeit.mission.deokhugam.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository bookRepository;
    private final BookImageService bookImageService;
    private final BookDtoMapper bookDtoMapper;
    private final WebClient webClient;
    private final ApplicationEventPublisher eventPublisher;

    private static final String NAVER_BOOK_API_URL = "https://openapi.naver.com/v1/search/book_adv";

    @Value("${naverapi.client-id}")
    private String naverClientId;
    @Value("${naverapi.client-secret}")
    private String naverClientSecret;
    @Value("${ocr.url}")
    private String ocrUrl;
    @Value("${ocr.apikey}")
    private String ocrApiKey;

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
                .uri(NAVER_BOOK_API_URL + "?d_isbn=" + isbn)
                .header("X-Naver-Client-Id", naverClientId)
                .header("X-Naver-Client-Secret", naverClientSecret)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        res -> Mono.error(new ExternalApiErrorException())
                )
                .onStatus(
                        status -> status.is5xxServerError(),
                        res -> Mono.error(new ExternalApiErrorException())
                )
                .bodyToMono(NaverResponseDto.class)
                .block(Duration.ofSeconds(5));

        //응답값이 없으면 예외 처리
        if(response == null ||  response.items() == null || response.items().isEmpty()) {
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
        if(imageUrl == null || imageUrl.isBlank()) return null;

        try{
            return webClient.get()
                    .uri(imageUrl)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block(Duration.ofSeconds(5));
        }catch (Exception e){
            //이미지 로딩 실패시 null
            return null;
        }
    }

    //이미지 기반 ISBN 인식 로직
    public String ocrIsbnDetect(MultipartFile image){
        OcrResponse response = webClient.post()
                .uri(ocrUrl)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("apikey", ocrApiKey)
                        .with("language", "eng")
                        .with("file", image.getResource()))
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        res -> Mono.error(new ExternalApiErrorException())
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        res -> Mono.error(new ExternalApiErrorException())
                )
                .bodyToMono(OcrResponse.class)
                .block(Duration.ofSeconds(5));

        if(response == null || response.parsedResults() == null || response.parsedResults().isEmpty()) {
            throw new ExternalApiErrorException();
        }

        if(response.parsedResults().get(0) == null) {
            throw new OcrFailedException();
        }

        String parsedText = response.parsedResults().get(0).parsedText();

        if(parsedText == null || parsedText.isBlank()) {
            throw new OcrFailedException();
        }

        return extractIsbn(parsedText);
    }

    private String extractIsbn(String text) {

        // 1. 라인 분리
        List<String> lines = Arrays.asList(text.split("\n"));

        // 2. ISBN 포함 라인 필터링
        List<String> candidateLines = lines.stream()
                .filter(line -> line.toUpperCase().contains("ISBN"))
                .toList();

        // fallback: 없으면 전체 라인 사용
        if (candidateLines.isEmpty()) {
            candidateLines = lines;
        }

        // 3. 정규식
        Pattern pattern = Pattern.compile("(97[89][- ]?\\d{1,5}[- ]?\\d+[- ]?\\d+[- ]?\\d)");

        for (String line : candidateLines) {

            // 4. 라인 단위 OCR 보정
            String normalizedLine = line
                    .replace("O", "0").replace("o", "0")
                    .replace("I", "1").replace("i", "1")
                    .replace("S", "5").replace("s", "5");

            Matcher matcher = pattern.matcher(normalizedLine);

            while (matcher.find()) {
                String raw = matcher.group();

                // 5. 숫자만 남기기
                String isbn = raw.replaceAll("[^0-9X]", "");

                // 6. 검증
                if (isbn.length() == 13 && isValidIsbn13(isbn)) {
                    return isbn;
                }
            }
        }

        throw new OcrFailedException();
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

    //책 상세 정보 조회 메서드
    public BookDto findBook(UUID id){
        Book book = bookRepository.findById(id).orElseThrow(BookNotFoundException::new);

        if(isDeleted(book)){
            throw new BookNotFoundException();
        }

        return bookDtoMapper.toDto(book);
    }

    //책 정보 수정 메서드
    @Transactional
    public BookDto updateBook(UUID id, BookUpdateRequest request, MultipartFile image){
        Book book = bookRepository.findById(id).orElseThrow(BookNotFoundException::new);

        if(isDeleted(book)){
            throw new BookNotFoundException();
        }

        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setDescription(request.description());
        book.setPublisher(request.publisher());
        book.setPublishedDate(request.publishedDate());

        if(image != null && !image.isEmpty()){
            bookImageService.deleteFileByUrl(book.getThumbnailUrl());
            book.setThumbnailUrl(upload(image));
        }

        bookRepository.save(book);

        return bookDtoMapper.toDto(book);
    }

    //책 데이터 논리 삭제 메서드
    @Transactional
    public void deleteBook(UUID id){
        Book book = bookRepository.findById(id).orElseThrow(BookNotFoundException::new);
        if(isDeleted(book)){
            throw new BookNotFoundException();
        }

        book.delete();
        bookRepository.save(book);
    }

    //도서 데이터 물리 삭제 메서드
    @Transactional
    public void hardDeleteBook(UUID id){
        Book book = bookRepository.findById(id).orElseThrow(BookNotFoundException::new);
        if(isDeleted(book)){
            throw new BookNotFoundException();
        }

        bookRepository.delete(book);

        eventPublisher.publishEvent(
                new BookDeletedEvent(book.getThumbnailUrl())
        );
    }

    private boolean isDeleted(Book book){
        return book.getBookStatus() == BookStatus.DELETED;
    }
}
