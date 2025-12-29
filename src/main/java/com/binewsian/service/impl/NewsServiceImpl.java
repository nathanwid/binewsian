package com.binewsian.service.impl;

import com.binewsian.constant.AppConstant;
import com.binewsian.dto.NewsRequest;
import com.binewsian.enums.NewsStatus;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Category;
import com.binewsian.model.News;
import com.binewsian.model.User;
import com.binewsian.repository.CategoryRepository;
import com.binewsian.repository.NewsRepository;
import com.binewsian.service.NewsService;
import com.binewsian.service.StorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final CategoryRepository categoryRepository;
    private final NewsRepository newsRepository;
    private final StorageService storageService;

    @Override
    public void create(NewsRequest request, MultipartFile featuredImage, User user) throws BiNewsianException {
        boolean isDraft = request.isDraft();

        validateRequest(request);

        Category category = null;

        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new BiNewsianException(AppConstant.CATEGORY_NOT_FOUND));
        }

        News news = new News();
        news.setTitle(request.title());
        news.setCategory(category);
        news.setSummary(request.summary());
        news.setContent(request.content());
        news.setStatus(isDraft ? NewsStatus.DRAFT : NewsStatus.PUBLISHED);
        news.setPublishedAt(isDraft ? null : LocalDateTime.now());
        news.setCreatedBy(user);

        processImage(news, featuredImage, isDraft, request.deleteImage());

        newsRepository.save(news);
    }

    @Override
    public void update(Long id, NewsRequest request, MultipartFile featuredImage, User user) throws BiNewsianException {
        boolean isDraft = request.isDraft();

        News news = newsRepository.findById(id)
                .orElseThrow(() -> new BiNewsianException(AppConstant.NEWS_NOT_FOUND));

        validateOwnerAndStatus(news, user);
        validateRequest(request);

        Category category = null;

        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new BiNewsianException(AppConstant.CATEGORY_NOT_FOUND));
        }
        
        news.setTitle(request.title());
        news.setCategory(category);
        news.setSummary(request.summary());
        news.setContent(request.content());
        news.setStatus(isDraft ? NewsStatus.DRAFT : NewsStatus.PUBLISHED);
        news.setPublishedAt(isDraft ? null : LocalDateTime.now());
        news.setCreatedBy(user);

        processImage(news, featuredImage, isDraft, request.deleteImage());

        newsRepository.save(news);
    }

    @Override
    public void delete(Long id) throws BiNewsianException {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new BiNewsianException(AppConstant.NEWS_NOT_FOUND));

        if (news.getFeaturedImageKey() != null) {
            storageService.deleteFile(news.getFeaturedImageKey());
        }

        newsRepository.delete(news);
    }

    @Override
    public News findById(Long id) throws BiNewsianException {
        return newsRepository.findById(id)
                .orElseThrow(() -> new BiNewsianException(AppConstant.NEWS_NOT_FOUND));
    }

    @Override
    public Page<News> findPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return newsRepository.findByStatus(NewsStatus.PUBLISHED, pageable);
    }

    @Override
    public Page<News> findPaginatedByUserId(int page, int size, long userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return newsRepository.findByCreatedBy_Id(userId, pageable);
    }

    @Override
    public List<News> findAllByStatus() {
        return newsRepository.findByStatusOrderByPublishedAtDesc(NewsStatus.PUBLISHED);
    }

    private void validateOwnerAndStatus(News news, User user) throws BiNewsianException {
        if (!news.getCreatedBy().getId().equals(user.getId())) {
            throw new BiNewsianException("You are not authorized to edit this activity.");
        }

        if (news.getStatus() == NewsStatus.PUBLISHED) {
            throw new BiNewsianException("Published activity cannot be edited.");
        }
    }

    private void validateRequest(NewsRequest r) throws BiNewsianException {
        String summary = r.summary();

        if (summary != null && summary.length() > 500) {
            throw new BiNewsianException("Summary cannot exceed 500 characters.");
        }

        if (!r.isDraft()) {
            String title = r.title();

            if (title == null || title.isBlank()) {
                throw new BiNewsianException("Title is required.");
            }

            if (r.categoryId() == null) {
                throw new BiNewsianException("Category is required.");
            }

            if (summary == null || summary.isBlank()) {
                throw new BiNewsianException("Summary is required.");
            }

            String content = r.content();

            if (content == null || content.isBlank()) {
                throw new BiNewsianException("Content cannot be empty.");
            }
        }
    }

    private void processImage(News news, MultipartFile file, boolean isDraft, boolean deleteImage) throws BiNewsianException {
        boolean hasFile = file != null && !file.isEmpty();
        String currentImageKey = news.getFeaturedImageKey();

        if (!isDraft && !hasFile && currentImageKey == null) {
            throw new BiNewsianException("Featured image is required for published news.");
        }

        if (currentImageKey != null && deleteImage) {
            storageService.deleteFile(currentImageKey);

            news.setFeaturedImageFileName(null);
            news.setFeaturedImageKey(null);
            news.setFeaturedImageUrl(null);
        }

        if (hasFile) {
            String contentType = file.getContentType();

            if (!contentType.startsWith("image/")) {
                throw new BiNewsianException("File must be an image.");
            }

            long maxSize = 5 * 1024 * 1024;

            if (file.getSize() > maxSize) {
                throw new BiNewsianException("Image size must not be greater than 5MB.");
            }

            String name = file.getOriginalFilename();
            String key = storageService.uploadFile(file);
            String url = storageService.getPublicUrl(key);

            news.setFeaturedImageFileName(name);
            news.setFeaturedImageKey(key);
            news.setFeaturedImageUrl(url);
        }
    }
}
