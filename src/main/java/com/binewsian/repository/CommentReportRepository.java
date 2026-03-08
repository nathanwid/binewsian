package com.binewsian.repository;

import com.binewsian.model.CommentReport;
import com.binewsian.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {

    boolean existsByComment_IdAndReportedBy(Long commentId, User reportedBy);

    long countByComment_Id(Long commentId);

    @Query("""
        select r.comment.id, count(r)
        from CommentReport r
        where r.comment.id in :commentIds
        group by r.comment.id
    """)
    List<Object[]> countByCommentIds(@Param("commentIds") List<Long> commentIds);

    @Query("""
        select r.comment.id
        from CommentReport r
        where r.reportedBy = :user and r.comment.id in :commentIds
    """)
    List<Long> findReportedCommentIds(@Param("user") User user,
                                      @Param("commentIds") List<Long> commentIds);

    @Query(
        value = """
            select r.comment.id, count(r), max(r.createdAt)
            from CommentReport r
            group by r.comment.id
            order by count(r) desc, max(r.createdAt) desc
        """,
        countQuery = """
            select count(distinct r.comment.id)
            from CommentReport r
            where r.comment.contentType = :contentType
        """
    )
    Page<Object[]> findReportedComments(Pageable pageable);

    @Query("""
        select r.comment.id, r.reason
        from CommentReport r
        where r.comment.id in :commentIds
          and r.createdAt = (
            select max(r2.createdAt)
            from CommentReport r2
            where r2.comment.id = r.comment.id
          )
    """)
    List<Object[]> findLatestReasonsByCommentIds(@Param("commentIds") List<Long> commentIds);
}
