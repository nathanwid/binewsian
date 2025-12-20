package com.binewsian.repository;

import com.binewsian.enums.ActivityStatus;
import com.binewsian.enums.ActivityType;
import com.binewsian.model.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    int countByStatus(ActivityStatus status);
    Page<Activity> findByStatus(ActivityStatus status, Pageable pageable);
}
