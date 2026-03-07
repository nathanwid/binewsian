package com.binewsian.repository;

import com.binewsian.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByContentIdAndContentTypeAndParentNull(Long contentId, String contentType, Pageable pageable);
    Page<Comment> findByParentId(Long parentId, Pageable pageable);
    long deleteByContentIdAndContentType(Long contentId, String contentType);

    @EntityGraph(attributePaths = {"user", "parent"})
    java.util.List<Comment> findByIdIn(java.util.List<Long> ids);
}
