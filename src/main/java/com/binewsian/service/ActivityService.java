package com.binewsian.service;

import com.binewsian.dto.CreateActivityRequest;
import com.binewsian.exception.BiNewsianException;

public interface ActivityService {
    void create(CreateActivityRequest request) throws BiNewsianException;
}
