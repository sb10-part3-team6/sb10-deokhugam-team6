package com.codeit.mission.deokhugam.book.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.codeit.mission.deokhugam.book.service.BookImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BookEventTest {

  @Test
  @DisplayName("이벤트가 발생하면 이미지가 삭제된다")
  void eventListenerTest() {
    // given
    BookImageService bookImageService = mock(BookImageService.class);
    BookDeletedEventListener listener =
        new BookDeletedEventListener(bookImageService);

    BookDeletedEvent event = new BookDeletedEvent("url");

    // when
    listener.handle(event);

    // then
    verify(bookImageService).deleteFileByUrl("url");
  }
}
