package com.binewsian.repository;

import com.binewsian.model.Bookmark;
import com.binewsian.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserAndContentTypeAndContentId(User user, String contentType, Long contentId);
    boolean existsByUserAndContentTypeAndContentId(User user, String contentType, Long contentId);
    List<Bookmark> findByUserAndContentTypeOrderByCreatedAtDesc(User user, String type);
    int countByUserAndContentType(User user, String type);
}

