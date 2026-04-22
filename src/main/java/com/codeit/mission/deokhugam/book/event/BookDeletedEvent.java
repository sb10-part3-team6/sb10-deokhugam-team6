package com.codeit.mission.deokhugam.book.event;

import lombok.Getter;

@Getter
public class BookDeletedEvent {
    private final String thumbnailUrl;

    public BookDeletedEvent(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}