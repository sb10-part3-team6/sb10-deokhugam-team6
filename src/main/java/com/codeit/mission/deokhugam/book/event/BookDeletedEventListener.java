package com.codeit.mission.deokhugam.book.event;

import com.codeit.mission.deokhugam.book.service.BookImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BookDeletedEventListener {
    private final BookImageService bookImageService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BookDeletedEvent event) {
        bookImageService.deleteFileByUrl(event.getThumbnailUrl());
    }
}