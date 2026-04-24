package com.codeit.mission.deokhugam.book.repository;

import com.codeit.mission.deokhugam.book.entity.Book;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, UUID>, BookRepositoryCustom {

  boolean existsByIsbn(String isbn);
}
