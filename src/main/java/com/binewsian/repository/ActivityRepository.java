package com.binewsian.repository;

import com.binewsian.enums.ActivityStatus;
import com.binewsian.enums.ActivityType;
import com.binewsian.model.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    int countByStatus(ActivityStatus status);
    int countByCreatedBy_Id(Long userId);
    int countByCreatedBy_IdAndStatus(Long userId, ActivityStatus status);
    Page<Activity> findByStatus(ActivityStatus status, Pageable pageable);
    Page<Activity> findByCreatedBy_Id(Long userId, Pageable pageable);
    List<Activity> findByStatusOrderByPublishedAtDesc(ActivityStatus status);
    Optional<Activity> findByIdAndStatus(Long id, ActivityStatus status);

    @Query("SELECT a FROM Activity a WHERE " +
            "a.status = :status " +
            "AND (CAST(:regDeadlineStart AS timestamp) IS NULL OR a.registrationDeadline > :regDeadlineStart) " +
            "AND (CAST(:regDeadlineEnd AS timestamp) IS NULL OR a.registrationDeadline <= :regDeadlineEnd) " +
            "AND (:includeOnline = true AND :includeOnsite = true OR " +
            "     :includeOnline = true AND LOWER(a.location) LIKE '%online%' OR " +
            "     :includeOnsite = true AND LOWER(a.location) NOT LIKE '%online%') " +
            "AND (:hasTypeFilter = false OR a.type IN :types) " +
            "AND (CAST(:dateFrom AS date) IS NULL OR a.activityDate >= :dateFrom) " +
            "AND (CAST(:dateTo AS date) IS NULL OR a.activityDate <= :dateTo)")
    Page<Activity> findActivitiesWithFilters(
            @Param("status") ActivityStatus status,
            @Param("regDeadlineStart") LocalDateTime regDeadlineStart,
            @Param("regDeadlineEnd") LocalDateTime regDeadlineEnd,
            @Param("includeOnline") boolean includeOnline,
            @Param("includeOnsite") boolean includeOnsite,
            @Param("hasTypeFilter") boolean hasTypeFilter,
            @Param("types") List<ActivityType> types,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            Pageable pageable
    );
}
