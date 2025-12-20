package com.binewsian.service.impl;

import com.binewsian.dto.CreateNewsRequest;
import com.binewsian.enums.NewsStatus;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.News;
import com.binewsian.repository.NewsRepository;
import com.binewsian.service.NewsService;
import com.binewsian.service.StorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final StorageService storageService;

    @Override
    public void create(CreateNewsRequest request, MultipartFile featuredImage) throws BiNewsianException {
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

        News news = new News();
        news.setTitle(request.title());
        news.setCategory(request.category());
        news.setSummary(request.summary());
        news.setContent(request.content());
        news.setFeaturedImageKey(key);
        news.setFeaturedImageUrl(publicUrl);
        news.setStatus(isDraft ? NewsStatus.DRAFT : NewsStatus.PUBLISHED);
        news.setPublishedAt(isDraft ? null : LocalDateTime.now());

        newsRepository.save(news);
    }

    private void validate(CreateNewsRequest r) throws BiNewsianException {
        if (r.title() == null || r.title().isBlank())
            throw new BiNewsianException("Title is required");

        if (r.category() == null)
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
