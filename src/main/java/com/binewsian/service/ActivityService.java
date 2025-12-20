package com.binewsian.service;

import com.binewsian.dto.CreateActivityRequest;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Activity;
import org.springframework.data.domain.Page;

public interface ActivityService {
    void create(CreateActivityRequest request) throws BiNewsianException;
    void delete(Long id) throws BiNewsianException;
    Page<Activity> findPaginated(int page, int size);
}
