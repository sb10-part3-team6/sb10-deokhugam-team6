package com.codeit.mission.deokhugam.book.service;

import com.codeit.mission.deokhugam.book.dto.BookCreateRequest;
import com.codeit.mission.deokhugam.book.dto.BookDto;
import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.book.exception.WrongFileTypeException;
import com.codeit.mission.deokhugam.book.mapper.BookDtoMapper;
import com.codeit.mission.deokhugam.book.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookDtoMapper bookDtoMapper;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookImageService bookImageService;

    @InjectMocks
    private BookService bookService;

    @Test
    @DisplayName("이미지 없이 도서를 생성하면 업로드 없이 저장된다")
    void createBookSuccessWithoutImage() {
        // given
        BookCreateRequest request = new BookCreateRequest(
                "제목", "저자", "설명", "출판사",
                LocalDate.now(), "isbn"
        );

        Book savedBook = Book.builder()
                .title("제목")
                .thumbnailUrl(null)
                .build();

        BookDto expectedDto = new BookDto(
                UUID.randomUUID(),
                "제목", "저자", "설명", "출판사",
                LocalDate.now(), "isbn", null, 0, 0.0,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
        when(bookDtoMapper.toDto(savedBook)).thenReturn(expectedDto);

        // when
        BookDto result = bookService.createBook(request, null);

        // then
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());

        Book saved = captor.getValue();

        assertThat(saved.getTitle()).isEqualTo("제목");
        assertThat(saved.getAuthor()).isEqualTo("저자");
        assertThat(saved.getDescription()).isEqualTo("설명");
        assertThat(saved.getPublisher()).isEqualTo("출판사");
        assertThat(saved.getPublishedDate()).isEqualTo(request.publishedDate());
        assertThat(saved.getIsbn()).isEqualTo("isbn");
        assertThat(saved.getThumbnailUrl()).isNull();
        assertThat(saved.getReviewCount()).isEqualTo(0);
        assertThat(saved.getRating()).isEqualTo(0.0);

        verify(bookImageService, never()).upload(any());
        verify(bookDtoMapper).toDto(savedBook);

        assertThat(result).isSameAs(expectedDto);
    }

    @Test
    @DisplayName("이미지와 함께 도서를 생성하면 업로드 후 저장된다")
    void createBookSuccessWithImage() {
        // given
        BookCreateRequest request = new BookCreateRequest(
                "제목", "저자", "설명", "출판사",
                LocalDate.now(), "isbn"
        );

        MultipartFile image = mock(MultipartFile.class);
        when(image.getContentType()).thenReturn("image/png");
        when(image.isEmpty()).thenReturn(false);

        String imageUrl = "https://image.url/test.png";

        Book savedBook = Book.builder()
                .title("제목")
                .thumbnailUrl(imageUrl)
                .build();

        BookDto expectedDto = new BookDto(
                UUID.randomUUID(),
                "제목", "저자", "설명", "출판사",
                LocalDate.now(), "isbn", imageUrl, 0, 0.0,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(bookImageService.upload(image)).thenReturn(imageUrl);
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
        when(bookDtoMapper.toDto(savedBook)).thenReturn(expectedDto);

        // when
        BookDto result = bookService.createBook(request, image);

        // then
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());

        Book saved = captor.getValue();

        assertThat(saved.getThumbnailUrl()).isEqualTo(imageUrl);
        assertThat(saved.getReviewCount()).isEqualTo(0);
        assertThat(saved.getRating()).isEqualTo(0.0);

        verify(bookImageService).upload(image);
        verify(bookDtoMapper).toDto(savedBook);

        assertThat(result).isSameAs(expectedDto);
    }

    @Test
    @DisplayName("이미지 업로드에 실패하면 도서 생성도 실패한다")
    void createBookFailWhenImageUploadFails() {
        // given
        BookCreateRequest request = new BookCreateRequest(
                "제목", "저자", "설명", "출판사",
                LocalDate.now(), "isbn"
        );

        MultipartFile image = mock(MultipartFile.class);

        //when & then
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn("image/jpeg");

        when(bookImageService.upload(image))
                .thenThrow(new RuntimeException("upload fail"));

        assertThatThrownBy(() -> bookService.createBook(request, image))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("upload fail");

        verify(bookRepository, never()).save(any());
        verify(bookDtoMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("이미지 타입이 아니면 예외가 발생한다")
    void createBookFailWhenWrongFileType() {
        // given
        BookCreateRequest request = new BookCreateRequest(
                "제목", "저자", "설명", "출판사",
                LocalDate.now(), "isbn"
        );

        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn("text/plain");

        // when & then
        assertThatThrownBy(() -> bookService.createBook(request, image))
                .isInstanceOf(WrongFileTypeException.class);

        verify(bookImageService, never()).upload(any());
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("DB 저장에 실패하면 예외가 발생한다")
    void createBookFailWhenRepositoryFails() {
        // given
        BookCreateRequest request = new BookCreateRequest(
                "제목", "저자", "설명", "출판사",
                LocalDate.now(), "isbn"
        );

        when(bookRepository.save(any(Book.class)))
                .thenThrow(new RuntimeException("DB 오류"));

        // when & then
        assertThatThrownBy(() -> bookService.createBook(request, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 오류");

        verify(bookDtoMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("api에서 반환된 텍스트를 정제해 isbn 값만 내보낸다: isbn 문구가 있을 때")
    void extractIsbnFromApiText_withIsbn() {
        //given
        String apiTest = "010-0000-0000\r\n0503405-5000\r\nubmedia@naver.com\r\nISBN 978-89-967241-5-5 (93010)\r\n";

        //when & then
        String result = ReflectionTestUtils.invokeMethod(bookService, "extractIsbn", apiTest);

        //then
        assertThat(result).isEqualTo("9788996724155");
    }

    @Test
    @DisplayName("api에서 반환된 텍스트를 정제해 isbn 값만 내보낸다: isbn 문구가 없을 때")
    void extractIsbnFromApiText_withoutIsbn(){
        //given
        String apiTest = "010-0000-0000\r\n0503405-5000\r\nubmedia@naver.com\r\n978-89-967241-5-5 (93010)\r\n";

        //when & then
        String result = ReflectionTestUtils.invokeMethod(bookService, "extractIsbn", apiTest);

        //then
        assertThat(result).isEqualTo("9788996724155");
    }


}
