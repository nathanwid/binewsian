package com.binewsian.repository;

import com.binewsian.enums.VoteType;
import com.binewsian.model.ForumThread;
import com.binewsian.model.ForumThreadVote;
import com.binewsian.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ForumThreadVoteRepository extends JpaRepository<ForumThreadVote, Long> {
    Optional<ForumThreadVote> findByThreadAndUser(ForumThread thread, User user);

    long countByThreadAndType(ForumThread thread, VoteType type);

    @Query("""
        select v.thread.id, count(v)
        from ForumThreadVote v
        where v.thread.id in :threadIds and v.type = :type
        group by v.thread.id
    """)
    List<Object[]> countByThreadIdsAndType(@Param("threadIds") List<Long> threadIds,
                                           @Param("type") VoteType type);

    @Query("""
        select v.thread.id, v.type
        from ForumThreadVote v
        where v.thread.id in :threadIds and v.user = :user
    """)
    List<Object[]> findUserVotesByThreadIds(@Param("threadIds") List<Long> threadIds,
                                            @Param("user") User user);

    void deleteByThread(ForumThread thread);
}
