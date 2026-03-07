package com.binewsian.service;

import com.binewsian.exception.BiNewsianException;
import com.binewsian.dto.ForumReportSummaryDto;
import com.binewsian.dto.ForumThreadPopularityDto;
import com.binewsian.dto.ForumVoteResponse;
import com.binewsian.model.ForumThread;
import com.binewsian.model.User;
import com.binewsian.enums.VoteType;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ForumService {

    List<ForumThread> findAllNewestFirst();
    Page<ForumThread> findPaginated(int page, int size, String search);
    Page<ForumReportSummaryDto> findReportedThreads(int page, int size);
    List<ForumThreadPopularityDto> getPopularThreads(int limit);
    List<ForumThreadPopularityDto> getMostCommentedThreads(int limit);
    List<ForumThreadPopularityDto> getMostLikedThreads(int limit);

    ForumThread getThreadById(Long threadId) throws BiNewsianException;

    ForumThread createThread(String title, String content, User user) throws BiNewsianException;

    void deleteThread(Long threadId, User user) throws BiNewsianException;

    void reportThread(Long threadId, User user, String reason) throws BiNewsianException;

    boolean hasReported(Long threadId, User user) throws BiNewsianException;

    ForumVoteResponse voteThread(Long threadId, User user, VoteType type) throws BiNewsianException;

    long countVotes(Long threadId, VoteType type) throws BiNewsianException;

    long countReports(Long threadId);

    VoteType getUserVote(Long threadId, User user) throws BiNewsianException;

    Map<Long, Long> countRepliesByThreadIds(List<Long> threadIds);

    Map<Long, Long> countVotesByThreadIds(List<Long> threadIds, VoteType type);

    Map<Long, VoteType> getUserVotesByThreadIds(User user, List<Long> threadIds);

    Map<Long, Long> countReportsByThreadIds(List<Long> threadIds);
}
