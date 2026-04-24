package com.codeit.mission.deokhugam.book.repository;

import com.codeit.mission.deokhugam.book.dto.BookSearchConditionDto;
import com.codeit.mission.deokhugam.book.entity.Book;
import java.util.List;

public interface BookRepositoryCustom {

  List<Book> findAllByCursor(BookSearchConditionDto condition);

  long countByCondition(String keyword);
}
