package com.binewsian.repository;

import com.binewsian.model.ForumThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForumThreadRepository extends JpaRepository<ForumThread, Long> {

    @Query("""
        select t from ForumThread t
        join fetch t.createdBy
        order by t.createdAt desc
    """)
    List<ForumThread> findAllNewestFirstWithUser();

    @Query("""
        select t from ForumThread t
        join fetch t.createdBy
        where t.id = :id
    """)
    Optional<ForumThread> findByIdWithUser(@Param("id") Long id);

    @NativeQuery("SELECT c.content_id, count(*) " +
            "FROM comments c " +
            "WHERE c.content_id IN :contentIds " +
            "AND c.content_type = 'THREAD' " +
            "AND c.is_deleted = false " +
            "GROUP BY c.content_id")
    List<Object[]> countByThreadIds(@Param("contentIds") List<Long> contentIds);

    @EntityGraph(attributePaths = {"createdBy"})
    Page<ForumThread> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy"})
    Page<ForumThread> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content, Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy"})
    List<ForumThread> findByIdIn(List<Long> ids);

    @Query(value = """
        SELECT t.id,
               COALESCE(c.comment_count, 0) AS comment_count,
               COALESCE(v.upvote_count, 0) AS upvote_count
        FROM forum_threads t
        LEFT JOIN (
            SELECT content_id, COUNT(*) AS comment_count
            FROM comments
            WHERE content_type = 'THREAD' AND is_deleted = false
            GROUP BY content_id
        ) c ON c.content_id = t.id
        LEFT JOIN (
            SELECT thread_id, COUNT(*) AS upvote_count
            FROM forum_thread_votes
            WHERE type = 'UP'
            GROUP BY thread_id
        ) v ON v.thread_id = t.id
        ORDER BY (COALESCE(c.comment_count, 0) + COALESCE(v.upvote_count, 0)) DESC, t.created_at DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findPopularThreadStats(@Param("limit") int limit);

    @Query(value = """
        SELECT t.id,
               COALESCE(c.comment_count, 0) AS comment_count,
               COALESCE(v.upvote_count, 0) AS upvote_count
        FROM forum_threads t
        LEFT JOIN (
            SELECT content_id, COUNT(*) AS comment_count
            FROM comments
            WHERE content_type = 'THREAD' AND is_deleted = false
            GROUP BY content_id
        ) c ON c.content_id = t.id
        LEFT JOIN (
            SELECT thread_id, COUNT(*) AS upvote_count
            FROM forum_thread_votes
            WHERE type = 'UP'
            GROUP BY thread_id
        ) v ON v.thread_id = t.id
        ORDER BY COALESCE(c.comment_count, 0) DESC, t.created_at DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findMostCommentedThreadStats(@Param("limit") int limit);

    @Query(value = """
        SELECT t.id,
               COALESCE(c.comment_count, 0) AS comment_count,
               COALESCE(v.upvote_count, 0) AS upvote_count
        FROM forum_threads t
        LEFT JOIN (
            SELECT content_id, COUNT(*) AS comment_count
            FROM comments
            WHERE content_type = 'THREAD' AND is_deleted = false
            GROUP BY content_id
        ) c ON c.content_id = t.id
        LEFT JOIN (
            SELECT thread_id, COUNT(*) AS upvote_count
            FROM forum_thread_votes
            WHERE type = 'UP'
            GROUP BY thread_id
        ) v ON v.thread_id = t.id
        ORDER BY COALESCE(v.upvote_count, 0) DESC, t.created_at DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findMostLikedThreadStats(@Param("limit") int limit);
}

