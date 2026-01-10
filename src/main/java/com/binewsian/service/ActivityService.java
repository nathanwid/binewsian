package com.binewsian.service;

import com.binewsian.dto.ActivityFilterDto;
import com.binewsian.dto.ActivityRequest;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Activity;
import com.binewsian.model.User;

import org.springframework.data.domain.Page;

import java.util.List;

public interface ActivityService {
    void create(ActivityRequest request, User user) throws BiNewsianException;
    void update(Long id, ActivityRequest request, User user) throws BiNewsianException;
    void delete(Long id) throws BiNewsianException;
    Activity findById(Long id) throws BiNewsianException;
    Page<Activity> findPaginated(int page, int size);
    Page<Activity> findPaginatedByUserId(int page, int size, Long userId);
    List<Activity> findAllByStatus();
    Page<Activity> getFilteredActivities(ActivityFilterDto filterDTO, int page, int size);
    Activity getActivityById(Long id) throws BiNewsianException;
}
