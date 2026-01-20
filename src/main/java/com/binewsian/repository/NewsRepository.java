package com.binewsian.repository;

import com.binewsian.enums.NewsStatus;
import com.binewsian.model.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {
    boolean existsById(Long id);
    int countByStatus(NewsStatus status);
    int countByCreatedBy_Id(Long userId);
    int countByCreatedBy_IdAndStatus(Long userId, NewsStatus status);
    Page<News> findByStatus(NewsStatus status, Pageable pageable);
    Page<News> findByCreatedBy_Id(Long userId, Pageable pageable);
    List<News> findByStatusOrderByPublishedAtDesc(NewsStatus status);
    List<News> findTop5ByPublishedAtNotNullOrderByPublishedAtDesc();

    @Query("SELECT n FROM News n WHERE " +
            "n.status = :status " +
            "AND (:categoryId IS NULL OR n.category.id = :categoryId)")
    Page<News> findNewsWithFilters(
            @Param("status") NewsStatus status,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );
}
