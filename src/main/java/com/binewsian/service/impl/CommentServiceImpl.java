package com.binewsian.service.impl;

import com.binewsian.enums.CommentableType;
import com.binewsian.model.Comment;
import com.binewsian.repository.CommentRepository;
import com.binewsian.service.CommentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Override
    public Page<Comment> findPaginated(int page, int size, Long commentableId, CommentableType commentableType) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return commentRepository.findByCommentableIdAndCommentableType(commentableId, commentableType, pageable);
    }
}
