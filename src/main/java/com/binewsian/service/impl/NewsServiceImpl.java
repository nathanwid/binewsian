package com.binewsian.service.impl;

import com.binewsian.constant.AppConstant;
import com.binewsian.dto.NewsFilterDto;
import com.binewsian.dto.NewsRequest;
import com.binewsian.enums.NewsStatus;
import com.binewsian.enums.Role;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Category;
import com.binewsian.model.News;
import com.binewsian.model.User;
import com.binewsian.repository.CategoryRepository;
import com.binewsian.repository.NewsRepository;
import com.binewsian.repository.UserRepository;
import com.binewsian.service.EmailService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final CategoryRepository categoryRepository;
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final EmailService emailService;

    @Override
    public void create(NewsRequest request, MultipartFile featuredImage, User user, String appUrl) throws BiNewsianException {
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

        News savedNews = newsRepository.save(news);

        if (!isDraft) {
            notifyUsers(savedNews, appUrl);
        }
    }

    @Override
    public void update(Long id, NewsRequest request, MultipartFile featuredImage, User user, String appUrl) throws BiNewsianException {
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

        processImage(news, featuredImage, isDraft, request.deleteImage());

        News savedNews = newsRepository.save(news);

        if (!isDraft) {
            notifyUsers(savedNews, appUrl);
        }
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
    public Page<News> findPaginatedByUserId(int page, int size, Long userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return newsRepository.findByCreatedBy_Id(userId, pageable);
    }

    @Override
    public List<News> findAllByStatus() {
        return newsRepository.findByStatusOrderByPublishedAtDesc(NewsStatus.PUBLISHED);
    }

    @Override
    public List<News> findAllByUserId(Long userId) {
        return newsRepository.findByCreatedBy_IdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<News> findLatestPublished() {
        return newsRepository.findTop5ByPublishedAtNotNullOrderByPublishedAtDesc();
    }

    @Override
    public Page<News> getFilteredNews(NewsFilterDto filterDto, int page, int size) {        
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "publishedAt");

        String categoryName = filterDto.getCategory();
        Long categoryId = null;
        
        if (categoryName != null && !categoryName.isEmpty()) {
            Optional<Category> categoryOpt = categoryRepository.findByNameIgnoreCase(categoryName);

            if (categoryOpt.isEmpty()) {
                return Page.empty(pageable);
            }

            categoryId = categoryOpt.get().getId();
        }

        return newsRepository.findNewsWithFilters(
                NewsStatus.PUBLISHED,
                categoryId,
                pageable
        );
    }

    private void notifyUsers(News news, String appUrl) throws BiNewsianException {
        List<User> users = userRepository.findByRoleAndEnabledTrue(Role.USER);

        Map<String, Object> data = new HashMap<>();
        data.put("contentType", "NEWS");
        data.put("author", news.getCreatedBy().getUsername());
        data.put("contentTitle", news.getTitle());
        data.put("contentDescription", news.getSummary());
        data.put("contentUrl", appUrl + "/news/" + news.getId());

        for (User user : users) {
            try {
                emailService.sendContentNotification(user, data);
            } catch (BiNewsianException e) {
                throw new BiNewsianException(e.getMessage());
            }
        }
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

        if (summary != null && summary.length() > 200) {
            throw new BiNewsianException("Summary cannot exceed 200 characters.");
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
