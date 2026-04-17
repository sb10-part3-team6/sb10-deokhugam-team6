package com.codeit.mission.deokhugam.book.repository;

import com.codeit.mission.deokhugam.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {
}
