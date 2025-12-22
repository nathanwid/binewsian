package com.binewsian.service.impl;

import com.binewsian.constant.AppConstant;
import com.binewsian.dto.CreateNewsRequest;
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

        String key = null;
        String publicUrl = null;
        if (featuredImage != null && !featuredImage.isEmpty()) {
            validateFeaturedImage(featuredImage);
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
        newsRepository.delete(news);
    }

    @Override
    public Page<News> findPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return newsRepository.findByStatus(NewsStatus.PUBLISHED, pageable);
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

    private void validateFeaturedImage(MultipartFile file) throws BiNewsianException {
        String contentType = file.getContentType();
        if (!contentType.startsWith("image/"))
            throw new BiNewsianException("File must be an image");

        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize)
            throw new BiNewsianException("Image size must not be greater than 5MB");
    }
}
