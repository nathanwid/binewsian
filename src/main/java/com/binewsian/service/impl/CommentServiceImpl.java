package com.binewsian.service.impl;

import com.binewsian.constant.AppConstant;
import com.binewsian.dto.CommentRequest;
import com.binewsian.enums.CommentableType;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Comment;
import com.binewsian.model.User;
import com.binewsian.repository.ActivityRepository;
import com.binewsian.repository.CommentRepository;
import com.binewsian.repository.NewsRepository;
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
    private final NewsRepository newsRepository;
    private final ActivityRepository activityRepository;

    public void create(CommentRequest request, User user) throws BiNewsianException {
        if (request.commentableType() == null) {
            throw new BiNewsianException("Commentable ID is required.");
        }

        if (request.commentableId() == null) {
            throw new BiNewsianException("Commentable type is required.");
        }

        switch (request.commentableType()) {
            case NEWS -> {
                if (!newsRepository.existsById(request.commentableId())) {
                    throw new BiNewsianException(AppConstant.NEWS_NOT_FOUND);
                }
            }
            case ACTIVITY -> {
                if (!activityRepository.existsById(request.commentableId())) {
                    throw new BiNewsianException(AppConstant.ACTIVITY_NOT_FOUND);
                }
            }
            case FORUM -> {
                
            }
        }

        if (request.content() == null || request.content().isBlank()) {
            throw new BiNewsianException("Content cannot be empty.");
        }

        Comment comment = new Comment();

        comment.setCommentableId(request.commentableId());
        comment.setCommentableType(request.commentableType());
        comment.setContent(request.content());
        comment.setCreatedBy(user);

        commentRepository.save(comment);
    }

    @Override
    public void update(Long id, User user) throws BiNewsianException {

    }

    @Override
    public void delete(Long id) throws BiNewsianException {

    }

    @Override
    public Page<Comment> findPaginated(int page, int size, Long commentableId, CommentableType commentableType) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return commentRepository.findByCommentableIdAndCommentableType(commentableId, commentableType, pageable);
    }
}
