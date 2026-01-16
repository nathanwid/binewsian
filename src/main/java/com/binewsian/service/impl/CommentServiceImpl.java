package com.binewsian.service.impl;

import com.binewsian.constant.AppConstant;
import com.binewsian.dto.CommentRequest;
import com.binewsian.enums.Role;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Comment;
import com.binewsian.model.User;
import com.binewsian.repository.ActivityRepository;
import com.binewsian.repository.CommentRepository;
import com.binewsian.repository.NewsRepository;
import com.binewsian.service.CommentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

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
        if (request.contentType() == null) {
            throw new BiNewsianException("Content type is required.");
        }

        if (request.contentId() == null) {
            throw new BiNewsianException("Content ID is required.");
        }

        switch (request.contentType()) {
            case "NEWS" -> {
                if (!newsRepository.existsById(request.contentId())) {
                    throw new BiNewsianException(AppConstant.NEWS_NOT_FOUND);
                }
            }
            case "ACTIVITY" -> {
                if (!activityRepository.existsById(request.contentId())) {
                    throw new BiNewsianException(AppConstant.ACTIVITY_NOT_FOUND);
                }
            }
        }

        if (request.content() == null || request.content().isBlank()) {
            throw new BiNewsianException("Content cannot be empty.");
        }

        Comment comment = new Comment();

        comment.setContentId(request.contentId());
        comment.setContentType(request.contentType());
        comment.setContent(request.content());
        comment.setUser(user);

        Long parentId = request.parentId();

        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new BiNewsianException(AppConstant.PARENT_COMMENT_NOT_FOUND));

            comment.setParent(parent);
        }

        commentRepository.save(comment);
    }

    @Override
    public void update(Long id, CommentRequest request, User user) throws BiNewsianException {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BiNewsianException(AppConstant.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new BiNewsianException("You are not authorized to edit this comment.");
        }

        comment.setContent(request.content());

        commentRepository.save(comment);
    }

    @Override
    public void delete(Long id, User user) throws BiNewsianException {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BiNewsianException(AppConstant.COMMENT_NOT_FOUND));
        
        if (!comment.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new BiNewsianException("You are not authorized to delete this comment.");
        }

        boolean isParent = comment.getParent() == null;
        boolean hasReplies = comment.getReplyCount() != 0;

        if (isParent && hasReplies) {
            comment.setDeleted(true);
            comment.setDeletedAt(LocalDateTime.now());
            commentRepository.save(comment);
        } else {
            Comment parent = comment.getParent();
            
            if (parent != null && parent.getDeleted() && parent.getReplyCount() <= 1) {
                commentRepository.delete(parent);
            }
            
            commentRepository.delete(comment);
        }
    }

    @Override
    public Page<Comment> findPaginated(int page, int size, Long contentId, String contentType) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return commentRepository.findByContentIdAndContentTypeAndParentNull(contentId, contentType, pageable);
    }

    @Override
    public Page<Comment> findReplies(int page, int size, Long parentId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return commentRepository.findByParentId(parentId, pageable);
    }
}
