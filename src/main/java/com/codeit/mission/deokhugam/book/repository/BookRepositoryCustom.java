package com.codeit.mission.deokhugam.book.repository;

import com.codeit.mission.deokhugam.book.entity.AssertDirection;
import com.codeit.mission.deokhugam.book.entity.Book;

import java.time.LocalDateTime;
import java.util.List;

public interface BookRepositoryCustom {
    List<Book> findAllByCursor(
            String keyword,
            String orderBy,
            AssertDirection direction,
            Object cursor,
            LocalDateTime after,
            int limit
    );
}
