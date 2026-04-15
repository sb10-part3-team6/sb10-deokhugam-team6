package com.codeit.mission.deokhugam.book.entity;

import com.codeit.mission.deokhugam.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Book extends BaseEntity {
    @Column(nullable = false)
    @Setter
    private String title;

    @Column(nullable = false)
    @Setter
    private String author;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Setter
    private String description;

    @Column(nullable = false)
    @Setter
    private String publisher;

    @Column(nullable = false)
    @Setter
    private LocalDate publishedDate;

    @Column
    private String isbn;

    @Column
    private String thumbnailUrl;

    @Column(nullable = false)
    private int reviewCount;

    @Column(nullable = false)
    private double rating;

    @Builder
    public Book(String title, String author, String description,
                String publisher, LocalDate publishedDate,
                String isbn, String thumbnailUrl,
                int reviewCount, double rating) {
        super();
        this.title = title;
        this.author = author;
        this.description = description;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.isbn = isbn;
        this.thumbnailUrl = thumbnailUrl;
        this.reviewCount = reviewCount;
        this.rating = rating;
    }

}
