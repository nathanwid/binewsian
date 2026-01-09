package com.binewsian.repository;

import com.binewsian.enums.ActivityStatus;
import com.binewsian.model.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    boolean existsById(Long id);
    int countByStatus(ActivityStatus status);
    int countByCreatedBy_Id(Long userId);
    int countByCreatedBy_IdAndStatus(Long userId, ActivityStatus status);
    Page<Activity> findByStatus(ActivityStatus status, Pageable pageable);
    Page<Activity> findByCreatedBy_Id(Long userId, Pageable pageable);
    List<Activity> findByStatusOrderByPublishedAtDesc(ActivityStatus status);
}
