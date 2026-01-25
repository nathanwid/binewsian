package com.binewsian.service.impl;

import com.binewsian.model.Activity;
import com.binewsian.model.Bookmark;
import com.binewsian.model.News;
import com.binewsian.model.User;
import com.binewsian.repository.ActivityRepository;
import com.binewsian.repository.BookmarkRepository;
import com.binewsian.repository.NewsRepository;
import com.binewsian.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ActivityRepository activityRepository;
    private final NewsRepository newsRepository;

    @Override
    @Transactional
    public boolean toggle(User user, String type, Long contentId) {
        return bookmarkRepository
                .findByUserAndContentTypeAndContentId(user, type, contentId)
                .map(existing -> {
                    bookmarkRepository.delete(existing);
                    return false; // removed
                })
                .orElseGet(() -> {
                    Bookmark bookmark = new Bookmark();
                    bookmark.setUser(user);
                    bookmark.setContentType(type);
                    bookmark.setContentId(contentId);
                    bookmarkRepository.save(bookmark);
                    return true; // saved
                });
    }

    @Override
    public boolean isBookmarked(User user, String type, Long contentId) {
        return bookmarkRepository.existsByUserAndContentTypeAndContentId(user, type, contentId);
    }

    @Override
    public List<Activity> getBookmarkedActivities(User user) {
        List<Long> activityIds = bookmarkRepository
                .findByUserAndContentTypeOrderByCreatedAtDesc(user, "ACTIVITY")
                .stream()
                .map(Bookmark::getContentId)
                .collect(Collectors.toList());

        return activityRepository.findAllById(activityIds);
    }

    @Override
    public List<News> getBookmarkedNews(User user) {
        List<Long> newsIds = bookmarkRepository
                .findByUserAndContentTypeOrderByCreatedAtDesc(user, "NEWS")
                .stream()
                .map(Bookmark::getContentId)
                .collect(Collectors.toList());

        return newsRepository.findAllById(newsIds);
    }

    @Override
    public int countByUserAndType(User user, String type) {
        return bookmarkRepository.countByUserAndContentType(user, type);
    }
}
