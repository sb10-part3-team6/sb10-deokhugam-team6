package com.codeit.mission.deokhugam.book.repository;

import com.codeit.mission.deokhugam.book.entity.Book;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, UUID>, BookRepositoryCustom {

  boolean existsByIsbn(String isbn);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("UPDATE Book b " +
      "SET b.reviewCount = b.reviewCount + 1, " +
      "    b.rating = (b.rating * b.reviewCount + :newRating) / (b.reviewCount + 1) " +
      "WHERE b.id = :bookId")
  void incrementReviewCountAndRating(@Param("bookId") UUID bookId, @Param("newRating") int newRating);

  // 리뷰 삭제 시: 개수 1 감소 및 평점 재계산 (0개 이하 방지 처리)
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("UPDATE Book b " +
      "SET b.rating = CASE WHEN b.reviewCount <= 1 THEN 0 " +
      "                    ELSE (b.rating * b.reviewCount - :removedRating) / (b.reviewCount - 1) END, " +
      "    b.reviewCount = CASE WHEN b.reviewCount > 0 THEN b.reviewCount - 1 ELSE 0 END " +
      "WHERE b.id = :bookId")
  void decrementReviewCountAndRating(@Param("bookId") UUID bookId, @Param("removedRating") int removedRating);
}
