package com.binewsian.service;

import com.binewsian.dto.NewsFilterDto;
import com.binewsian.dto.NewsRequest;
import com.binewsian.exception.BiNewsianException;

import com.binewsian.model.News;
import com.binewsian.model.User;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NewsService {
    void create(NewsRequest request, MultipartFile featuredImage, User user, String appUrl) throws BiNewsianException;
    void update(Long id, NewsRequest request, MultipartFile featuredImage, User user, String appUrl) throws BiNewsianException;
    void delete(Long id) throws BiNewsianException;
    News findById(Long id) throws BiNewsianException;
    Page<News> findPaginated(int page, int size);
    Page<News> findPaginatedByUserId(int page, int size, Long userId);
    List<News> findAllByStatus();
    List<News> findAllByUserId(Long userId);
    List<News> findLatestPublished();
    Page<News> getFilteredNews(NewsFilterDto filterDto, int page, int size);
}
