package com.binewsian.service;

import com.binewsian.dto.CommentRequest;
import com.binewsian.dto.NewsRequest;
import com.binewsian.enums.CommentableType;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Comment;
import com.binewsian.model.User;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface CommentService {
    void create(CommentRequest request, User user) throws BiNewsianException;
    void update(Long id, User user) throws BiNewsianException;
    void delete(Long id) throws BiNewsianException;
    Page<Comment> findPaginated(int page, int size, Long commentableId, CommentableType commentableType);
}
