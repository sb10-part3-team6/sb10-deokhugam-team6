package com.codeit.mission.deokhugam.book.mapper;

import com.codeit.mission.deokhugam.book.dto.response.BookDto;
import com.codeit.mission.deokhugam.book.entity.Book;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface BookDtoMapper {

  BookDto toDto(Book book);
}
