package com.binewsian.service.impl;

import com.binewsian.constant.AppConstant;
import com.binewsian.dto.ForumReportSummaryDto;
import com.binewsian.dto.ForumThreadPopularityDto;
import com.binewsian.dto.ForumVoteResponse;
import com.binewsian.enums.Role;
import com.binewsian.enums.VoteType;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.ForumThread;
import com.binewsian.model.ForumThreadReport;
import com.binewsian.model.ForumThreadVote;
import com.binewsian.model.User;
import com.binewsian.repository.CommentRepository;
import com.binewsian.repository.ForumThreadRepository;
import com.binewsian.repository.ForumThreadReportRepository;
import com.binewsian.repository.ForumThreadVoteRepository;
import com.binewsian.service.ForumService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class ForumServiceImpl implements ForumService {

    private final ForumThreadRepository forumThreadRepository;
    private final ForumThreadVoteRepository forumThreadVoteRepository;
    private final CommentRepository commentRepository;
    private final ForumThreadReportRepository forumThreadReportRepository;

    @Override
    public List<ForumThread> findAllNewestFirst() {
        return forumThreadRepository.findAllNewestFirstWithUser();
    }

    @Override
    public Page<ForumThread> findPaginated(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (search == null || search.isBlank()) {
            return forumThreadRepository.findAll(pageable);
        }
        String term = search.trim();
        return forumThreadRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(term, term, pageable);
    }

    @Override
    public Page<ForumReportSummaryDto> findReportedThreads(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> reportedRows = forumThreadReportRepository.findReportedThreadSummaries(pageable);

        List<Long> threadIds = reportedRows.getContent().stream()
                .map(row -> ((Number) row[0]).longValue())
                .toList();

        if (threadIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, reportedRows.getTotalElements());
        }

        List<ForumThread> threads = forumThreadRepository.findByIdIn(threadIds);
        Map<Long, ForumThread> threadMap = threads.stream()
                .collect(Collectors.toMap(ForumThread::getId, t -> t));

        Map<Long, String> latestReasons = new java.util.HashMap<>();
        for (Object[] row : forumThreadReportRepository.findLatestReasonsByThreadIds(threadIds)) {
            Long threadId = ((Number) row[0]).longValue();
            String reason = (String) row[1];
            latestReasons.put(threadId, reason);
        }

        List<ForumReportSummaryDto> results = reportedRows.getContent().stream()
                .map(row -> {
                    Long threadId = ((Number) row[0]).longValue();
                    long count = ((Number) row[1]).longValue();
                    java.time.LocalDateTime latestAt = (java.time.LocalDateTime) row[2];
                    ForumThread thread = threadMap.get(threadId);
                    if (thread == null) {
                        return null;
                    }
                    return new ForumReportSummaryDto(
                            thread,
                            count,
                            latestReasons.get(threadId),
                            latestAt
                    );
                })
                .filter(r -> r != null)
                .toList();

        return new PageImpl<>(results, pageable, reportedRows.getTotalElements());
    }

    @Override
    public List<ForumThreadPopularityDto> getPopularThreads(int limit) {
        return mapPopularityRows(forumThreadRepository.findPopularThreadStats(limit));
    }

    @Override
    public List<ForumThreadPopularityDto> getMostCommentedThreads(int limit) {
        return mapPopularityRows(forumThreadRepository.findMostCommentedThreadStats(limit));
    }

    @Override
    public List<ForumThreadPopularityDto> getMostLikedThreads(int limit) {
        return mapPopularityRows(forumThreadRepository.findMostLikedThreadStats(limit));
    }

    @Override
    public ForumThread getThreadById(Long threadId) throws BiNewsianException {
        return forumThreadRepository.findByIdWithUser(threadId)
                .orElseThrow(() -> new BiNewsianException("Forum thread not found"));
    }

    @Override
    public ForumThread createThread(String title, String content, User user) throws BiNewsianException {
        validateUser(user);
        validateTitleAndContent(title, content);

        ForumThread thread = new ForumThread();
        thread.setTitle(title.trim());
        thread.setContent(content.trim());
        thread.setCreatedBy(user);
        thread.setCreatedAt(LocalDateTime.now());
        thread.setUpdatedAt(LocalDateTime.now());

        return forumThreadRepository.save(thread);
    }

    @Override
    public void deleteThread(Long threadId, User user) throws BiNewsianException {
        validateUser(user);

        ForumThread thread = getThreadById(threadId);

        if (user.getRole() != Role.ADMIN && !thread.getCreatedBy().getId().equals(user.getId())) {
            throw new BiNewsianException(AppConstant.UNAUTHORIZED_ACCESS);
        }

        forumThreadVoteRepository.deleteByThread(thread);
        forumThreadReportRepository.deleteByThread(thread);
        commentRepository.deleteByContentIdAndContentType(threadId, "THREAD");
        forumThreadRepository.delete(thread);
    }

    @Override
    public void reportThread(Long threadId, User user, String reason) throws BiNewsianException {
        validateUser(user);
        ForumThread thread = getThreadById(threadId);

        if (forumThreadReportRepository.existsByThreadAndReportedBy(thread, user)) {
            throw new BiNewsianException("You have already reported this thread.");
        }

        ForumThreadReport report = new ForumThreadReport();
        report.setThread(thread);
        report.setReportedBy(user);

        if (reason != null) {
            String cleaned = reason.trim();
            if (!cleaned.isEmpty()) {
                report.setReason(cleaned.length() > 255 ? cleaned.substring(0, 255) : cleaned);
            }
        }

        forumThreadReportRepository.save(report);
    }

    @Override
    public boolean hasReported(Long threadId, User user) throws BiNewsianException {
        if (user == null || user.getId() == null) {
            return false;
        }
        ForumThread thread = getThreadById(threadId);
        return forumThreadReportRepository.existsByThreadAndReportedBy(thread, user);
    }

    @Override
    public ForumVoteResponse voteThread(Long threadId, User user, VoteType type) throws BiNewsianException {
        validateUser(user);
        ForumThread thread = getThreadById(threadId);

        ForumThreadVote existing = forumThreadVoteRepository
                .findByThreadAndUser(thread, user)
                .orElse(null);

        VoteType userVote = null;

        if (existing == null) {
            ForumThreadVote vote = new ForumThreadVote();
            vote.setThread(thread);
            vote.setUser(user);
            vote.setType(type);
            forumThreadVoteRepository.save(vote);
            userVote = type;
        } else if (existing.getType() == type) {
            forumThreadVoteRepository.delete(existing);
            userVote = null;
        } else {
            existing.setType(type);
            forumThreadVoteRepository.save(existing);
            userVote = type;
        }

        long upvotes = forumThreadVoteRepository.countByThreadAndType(thread, VoteType.UP);
        long downvotes = forumThreadVoteRepository.countByThreadAndType(thread, VoteType.DOWN);

        return new ForumVoteResponse(upvotes, downvotes, userVote);
    }

    @Override
    public long countVotes(Long threadId, VoteType type) throws BiNewsianException {
        ForumThread thread = getThreadById(threadId);
        return forumThreadVoteRepository.countByThreadAndType(thread, type);
    }

    @Override
    public long countReports(Long threadId) {
        return forumThreadReportRepository.countByThreadId(threadId);
    }

    @Override
    public VoteType getUserVote(Long threadId, User user) throws BiNewsianException {
        if (user == null || user.getId() == null) {
            return null;
        }
        ForumThread thread = getThreadById(threadId);
        return forumThreadVoteRepository.findByThreadAndUser(thread, user)
                .map(ForumThreadVote::getType)
                .orElse(null);
    }

    @Override
    public Map<Long, Long> countRepliesByThreadIds(List<Long> threadIds) {
        return forumThreadRepository.countByThreadIds(threadIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));
    }

    @Override
    public Map<Long, Long> countVotesByThreadIds(List<Long> threadIds, VoteType type) {
        if (threadIds == null || threadIds.isEmpty()) {
            return Map.of();
        }
        return forumThreadVoteRepository.countByThreadIdsAndType(threadIds, type)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Override
    public Map<Long, VoteType> getUserVotesByThreadIds(User user, List<Long> threadIds) {
        if (user == null || user.getId() == null || threadIds == null || threadIds.isEmpty()) {
            return Map.of();
        }
        return forumThreadVoteRepository.findUserVotesByThreadIds(threadIds, user)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (VoteType) row[1]
                ));
    }

    @Override
    public Map<Long, Long> countReportsByThreadIds(List<Long> threadIds) {
        if (threadIds == null || threadIds.isEmpty()) {
            return Map.of();
        }
        return forumThreadReportRepository.countByThreadIds(threadIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));
    }

    private List<ForumThreadPopularityDto> mapPopularityRows(List<Object[]> rows) {
        List<Long> threadIds = rows.stream()
                .map(row -> ((Number) row[0]).longValue())
                .toList();

        if (threadIds.isEmpty()) {
            return List.of();
        }

        List<ForumThread> threads = forumThreadRepository.findByIdIn(threadIds);
        Map<Long, ForumThread> threadMap = threads.stream()
                .collect(Collectors.toMap(ForumThread::getId, t -> t));

        return rows.stream()
                .map(row -> {
                    Long threadId = ((Number) row[0]).longValue();
                    long commentCount = ((Number) row[1]).longValue();
                    long upvoteCount = ((Number) row[2]).longValue();
                    ForumThread thread = threadMap.get(threadId);
                    if (thread == null) {
                        return null;
                    }
                    long score = commentCount + upvoteCount;
                    return new ForumThreadPopularityDto(thread, commentCount, upvoteCount, score);
                })
                .filter(r -> r != null)
                .toList();
    }

    private void validateUser(User user) throws BiNewsianException {
        if (user == null || user.getId() == null) {
            throw new BiNewsianException(AppConstant.UNAUTHORIZED_ACCESS);
        }
        if (!user.isEnabled()) {
            throw new BiNewsianException(AppConstant.USER_HAS_BEEN_DEACTIVATED);
        }
    }

    private void validateTitleAndContent(String title, String content) throws BiNewsianException {
        if (title == null || title.trim().isEmpty()) {
            throw new BiNewsianException("Title cannot be empty");
        }
        if (title.trim().length() > 255) {
            throw new BiNewsianException("Title is too long (max 255 chars)");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BiNewsianException("Content cannot be empty");
        }
    }
}
