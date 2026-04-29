package com.codeit.mission.deokhugam.book.entity;

import com.codeit.mission.deokhugam.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

  @Column(nullable = false, unique = true)
  private String isbn;

  @Column
  @Setter
  private String thumbnailUrl;

  @Column(nullable = false)
  @Min(0)
  private int reviewCount;

  @Column(nullable = false)
  @Min(0)
  @Max(5)
  private double rating;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private BookStatus bookStatus;

  @Column
  private Instant deletedAt;

  //빌더 패턴 적용
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
    this.bookStatus = BookStatus.ACTIVE;
  }

  //리뷰 추가
  public void addReview(int review) {
    this.rating = (this.rating * this.reviewCount + review) / (this.reviewCount + 1);
    this.reviewCount++;
  }

  //리뷰 삭제 (물리 삭제 시 활용)
  public void removeReview(int review) {
    if (this.reviewCount <= 1) {
      this.reviewCount = 0;
      this.rating = 0;
      return;
    }

    this.rating = (this.rating * this.reviewCount - review) / (this.reviewCount - 1);
    this.reviewCount--;
  }

  //논리 삭제 메서드
  public void delete() {
    this.bookStatus = BookStatus.DELETED;
    this.deletedAt = Instant.now();
  }
}
