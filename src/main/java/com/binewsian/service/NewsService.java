package com.binewsian.service;

import com.binewsian.dto.CreateNewsRequest;
import com.binewsian.exception.BiNewsianException;
import org.springframework.web.multipart.MultipartFile;

public interface NewsService {
    void create(CreateNewsRequest request, MultipartFile featuredImage) throws BiNewsianException;
}
