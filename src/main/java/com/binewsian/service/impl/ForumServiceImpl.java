package com.binewsian.service.impl;

import com.binewsian.constant.AppConstant;
import com.binewsian.dto.ForumVoteResponse;
import com.binewsian.enums.VoteType;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.ForumThread;
import com.binewsian.model.ForumThreadVote;
import com.binewsian.model.User;
import com.binewsian.repository.CommentRepository;
import com.binewsian.repository.ForumThreadRepository;
import com.binewsian.repository.ForumThreadVoteRepository;
import com.binewsian.service.ForumService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
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

    @Override
    public List<ForumThread> findAllNewestFirst() {
        return forumThreadRepository.findAllNewestFirstWithUser();
    }

    @Override
    public Page<ForumThread> findPaginated(int page, int size) {
        return forumThreadRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
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

        if (!thread.getCreatedBy().getId().equals(user.getId())) {
            throw new BiNewsianException(AppConstant.UNAUTHORIZED_ACCESS);
        }

        forumThreadVoteRepository.deleteByThread(thread);
        commentRepository.deleteByContentIdAndContentType(threadId, "THREAD");
        forumThreadRepository.delete(thread);
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
