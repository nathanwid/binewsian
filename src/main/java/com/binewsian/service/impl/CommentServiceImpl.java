package com.binewsian.service.impl;

import com.binewsian.constant.AppConstant;
import com.binewsian.dto.CommentRequest;
import com.binewsian.enums.Role;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Comment;
import com.binewsian.model.User;
import com.binewsian.repository.ActivityRepository;
import com.binewsian.repository.CommentRepository;
import com.binewsian.repository.CommentReportRepository;
import com.binewsian.repository.NewsRepository;
import com.binewsian.service.CommentService;
import com.binewsian.dto.CommentReportSummaryDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentReportRepository commentReportRepository;
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

    @Override
    public void deleteByAdmin(Long id, User user) throws BiNewsianException {
        if (user == null || user.getId() == null) {
            throw new BiNewsianException(AppConstant.UNAUTHORIZED_ACCESS);
        }
        if (user.getRole() != Role.ADMIN) {
            throw new BiNewsianException(AppConstant.UNAUTHORIZED_ACCESS);
        }
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BiNewsianException(AppConstant.COMMENT_NOT_FOUND));

        comment.setDeleted(true);
        comment.setDeletedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    @Override
    public void report(Long id, User user, String reason) throws BiNewsianException {
        if (user == null || user.getId() == null) {
            throw new BiNewsianException(AppConstant.UNAUTHORIZED_ACCESS);
        }
        if (!user.isEnabled()) {
            throw new BiNewsianException(AppConstant.USER_HAS_BEEN_DEACTIVATED);
        }

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BiNewsianException(AppConstant.COMMENT_NOT_FOUND));

        if (commentReportRepository.existsByComment_IdAndReportedBy(id, user)) {
            throw new BiNewsianException("You have already reported this comment.");
        }

        var report = new com.binewsian.model.CommentReport();
        report.setComment(comment);
        report.setReportedBy(user);

        if (reason != null) {
            String cleaned = reason.trim();
            if (!cleaned.isEmpty()) {
                report.setReason(cleaned.length() > 255 ? cleaned.substring(0, 255) : cleaned);
            }
        }

        commentReportRepository.save(report);
    }

    @Override
    public long countReports(Long id) {
        return commentReportRepository.countByComment_Id(id);
    }

    @Override
    public Map<Long, Long> countReportsByCommentIds(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return Map.of();
        }
        return commentReportRepository.countByCommentIds(commentIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));
    }

    @Override
    public List<Long> getUserReportedCommentIds(User user, List<Long> commentIds) {
        if (user == null || user.getId() == null || commentIds == null || commentIds.isEmpty()) {
            return List.of();
        }
        return commentReportRepository.findReportedCommentIds(user, commentIds);
    }

    @Override
    public Page<CommentReportSummaryDto> findReportedComments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> rows = commentReportRepository.findReportedComments(pageable);

        List<Long> commentIds = rows.getContent().stream()
                .map(row -> ((Number) row[0]).longValue())
                .toList();

        if (commentIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, rows.getTotalElements());
        }

        List<Comment> comments = commentRepository.findByIdIn(commentIds);
        Map<Long, Comment> commentMap = comments.stream()
                .collect(Collectors.toMap(Comment::getId, c -> c));

        Map<Long, String> latestReasons = commentReportRepository.findLatestReasonsByCommentIds(commentIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> (String) row[1],
                        (a, b) -> a
                ));

        List<CommentReportSummaryDto> results = rows.getContent().stream()
                .map(row -> {
                    Long commentId = ((Number) row[0]).longValue();
                    long count = ((Number) row[1]).longValue();
                    LocalDateTime latestAt = (LocalDateTime) row[2];
                    Comment comment = commentMap.get(commentId);
                    if (comment == null) {
                        return null;
                    }
                    return new CommentReportSummaryDto(
                            comment,
                            count,
                            latestReasons.get(commentId),
                            latestAt
                    );
                })
                .filter(r -> r != null)
                .toList();

        return new PageImpl<>(results, pageable, rows.getTotalElements());
    }
}
