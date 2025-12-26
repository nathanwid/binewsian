package com.binewsian.service.impl;

import com.binewsian.constant.AppConstant;
import com.binewsian.dto.CreateNewsRequest;
import com.binewsian.dto.UpdateNewsRequest;
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
    public void create(CreateNewsRequest request, MultipartFile featuredImage, User user) throws BiNewsianException {
        boolean isDraft = request.isDraft();
        if (!isDraft) {
            validate(request);
            if (featuredImage == null || featuredImage.isEmpty()) {
                throw new BiNewsianException("Featured image is required for published news");
            }
        }
        
        String fileName = null;
        String key = null;
        String publicUrl = null;

        if (featuredImage != null && !featuredImage.isEmpty()) {
            validateFeaturedImage(featuredImage);
            fileName = featuredImage.getOriginalFilename();
            key = storageService.uploadFile(featuredImage);
            publicUrl = storageService.getPublicUrl(key);
        }

        Long categoryId = request.categoryId();
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BiNewsianException(AppConstant.CATEGORY_NOT_FOUND));
        }

        News news = new News();
        news.setTitle(request.title());
        news.setCategory(category);
        news.setSummary(request.summary());
        news.setContent(request.content());
        news.setFeaturedImageFileName(fileName);
        news.setFeaturedImageKey(key);
        news.setFeaturedImageUrl(publicUrl);
        news.setStatus(isDraft ? NewsStatus.DRAFT : NewsStatus.PUBLISHED);
        news.setPublishedAt(isDraft ? null : LocalDateTime.now());
        news.setCreatedBy(user);

        newsRepository.save(news);
    }

    @Override
    public void update(Long id, UpdateNewsRequest request, MultipartFile featuredImage, User user) throws BiNewsianException {
        News news = newsRepository.findById(id).orElseThrow(() -> new BiNewsianException(AppConstant.NEWS_NOT_FOUND));

        if (!news.getCreatedBy().getId().equals(user.getId())) {
            throw new BiNewsianException("You are not authorized to edit this activity.");
        }

        if (news.getStatus() == NewsStatus.PUBLISHED) {
            throw new BiNewsianException("Published activity cannot be edited.");
        }

        boolean isDraft = request.isDraft();
        if (!isDraft) {
            validateUpdate(request);
            if (featuredImage == null || featuredImage.isEmpty()) {
                throw new BiNewsianException("Featured image is required for published news");
            }
        }

        System.out.println("[Debug #1] Featured Image: " + featuredImage);

        String oldImageKey = news.getFeaturedImageKey();
        if (request.deleteImage() && oldImageKey != null) {
            storageService.deleteFile(oldImageKey);
        }

        System.out.println("[Debug #2] Featured Image: " + featuredImage);

        String fileName = null;
        String key = null;
        String publicUrl = null;

        if (featuredImage != null && !featuredImage.isEmpty()) {
            validateFeaturedImage(featuredImage);
            fileName = featuredImage.getOriginalFilename();
            key = storageService.uploadFile(featuredImage);
            publicUrl = storageService.getPublicUrl(key);
        } 

        Long categoryId = request.categoryId();
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BiNewsianException(AppConstant.CATEGORY_NOT_FOUND));
        }

        news.setTitle(request.title());
        news.setCategory(category);
        news.setSummary(request.summary());
        news.setContent(request.content());
        news.setFeaturedImageFileName(fileName);
        news.setFeaturedImageKey(key);
        news.setFeaturedImageUrl(publicUrl);
        news.setStatus(isDraft ? NewsStatus.DRAFT : NewsStatus.PUBLISHED);
        news.setPublishedAt(isDraft ? null : LocalDateTime.now());
        news.setCreatedBy(user);

        newsRepository.save(news);
    }

    @Override
    public void delete(Long id) throws BiNewsianException {
        News news = newsRepository.findById(id).orElseThrow(() -> new BiNewsianException(AppConstant.NEWS_NOT_FOUND));

        if (news.getFeaturedImageKey() != null) {
            storageService.deleteFile(news.getFeaturedImageKey());
        }

        newsRepository.delete(news);
    }

    @Override
    public News findById(Long id) throws BiNewsianException {
        return newsRepository.findById(id).orElseThrow(() -> new BiNewsianException(AppConstant.NEWS_NOT_FOUND));
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

    private void validate(CreateNewsRequest r) throws BiNewsianException {
        if (r.title() == null || r.title().isBlank())
            throw new BiNewsianException("Title is required");

        if (r.categoryId() == null)
            throw new BiNewsianException("Category is required");

        if (r.summary() == null || r.summary().isBlank())
            throw new BiNewsianException("Summary is required");

        if (r.content() == null || r.content().isBlank())
            throw new BiNewsianException("Details cannot be empty");
    }

    private void validateUpdate(UpdateNewsRequest r) throws BiNewsianException {
        if (r.title() == null || r.title().isBlank())
            throw new BiNewsianException("Title is required");

        if (r.categoryId() == null)
            throw new BiNewsianException("Category is required");

        if (r.summary() == null || r.summary().isBlank())
            throw new BiNewsianException("Summary is required");

        if (r.content() == null || r.content().isBlank())
            throw new BiNewsianException("Details cannot be empty");

        if (r.deleteImage() == null) {
            throw new BiNewsianException("Featured image is required for published news");
        }
    }

    private void validateFeaturedImage(MultipartFile file) throws BiNewsianException {
        String contentType = file.getContentType();
        if (!contentType.startsWith("image/"))
            throw new BiNewsianException("File must be an image");

        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize)
            throw new BiNewsianException("Image size must not be greater than 5MB");
    }
}
