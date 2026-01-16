package com.binewsian.repository;

import com.binewsian.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByContentIdAndContentTypeAndParentNull(Long contentId, String contentType, Pageable pageable);
    Page<Comment> findByParentId(Long parentId, Pageable pageable);
}
