package com.codeit.mission.deokhugam.book.entity;

import com.codeit.mission.deokhugam.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Setter
@Getter
public class Book extends BaseEntity {
    @Column(nullable = false)
    String title;

    @Column(nullable = false)
    String author;

    @Column(nullable = false)
    String description;

    @Column(nullable = false)
    String publisher;

    @Column(nullable = false)
    LocalDate publishedDate;

    @Column
    String isbn;

    @Column
    String thumbnailUrl;

    @Column(nullable = false)
    long reviewCount;

    @Column(nullable = false)
    BigDecimal rating;

}
