package com.binewsian.service;

import com.binewsian.dto.CommentRequest;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Comment;
import com.binewsian.model.User;
import org.springframework.data.domain.Page;

public interface CommentService {
    void create(CommentRequest request, User user) throws BiNewsianException;
    void update(Long id, CommentRequest request, User user) throws BiNewsianException;
    void delete(Long id, User user) throws BiNewsianException;
    Page<Comment> findPaginated(int page, int size, Long contentId, String contentType);
    Page<Comment> findReplies(int page, int size, Long commentId);
}
