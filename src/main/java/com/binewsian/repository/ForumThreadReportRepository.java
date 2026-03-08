package com.binewsian.repository;

import com.binewsian.model.ForumThread;
import com.binewsian.model.ForumThreadReport;
import com.binewsian.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ForumThreadReportRepository extends JpaRepository<ForumThreadReport, Long> {

    boolean existsByThreadAndReportedBy(ForumThread thread, User reportedBy);

    long countByThreadId(Long threadId);

    void deleteByThread(ForumThread thread);

    @Query("""
        select r.thread.id, count(r)
        from ForumThreadReport r
        where r.thread.id in :threadIds
        group by r.thread.id
    """)
    List<Object[]> countByThreadIds(@Param("threadIds") List<Long> threadIds);

    @Query(
        value = """
            select r.thread.id, count(r), max(r.createdAt)
            from ForumThreadReport r
            group by r.thread.id
            order by count(r) desc, max(r.createdAt) desc
        """,
        countQuery = "select count(distinct r.thread.id) from ForumThreadReport r"
    )
    Page<Object[]> findReportedThreadSummaries(Pageable pageable);

    @Query("""
        select r.thread.id, r.reason
        from ForumThreadReport r
        where r.thread.id in :threadIds
          and r.createdAt = (
            select max(r2.createdAt)
            from ForumThreadReport r2
            where r2.thread.id = r.thread.id
          )
    """)
    List<Object[]> findLatestReasonsByThreadIds(@Param("threadIds") List<Long> threadIds);
}
