package com.binewsian.service.impl;

import com.binewsian.model.*;
import com.binewsian.repository.ActivityRepository;
import com.binewsian.repository.BookmarkRepository;
import com.binewsian.repository.ForumThreadRepository;
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
    private final ForumThreadRepository forumThreadRepository;

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
        return activityRepository.findAllById(getContentIds(user, "ACTIVITY"));
    }

    @Override
    public List<News> getBookmarkedNews(User user) {
        return newsRepository.findAllById(getContentIds(user, "NEWS"));
    }

    @Override
    public List<ForumThread> getBookmarkedThreads(User user) {
        return forumThreadRepository.findAllById(getContentIds(user, "THREAD"));
    }

    @Override
    public List<Long> getBookmarkedForumThreadIds(User user) {
        return bookmarkRepository
                .findByUserAndContentTypeOrderByCreatedAtDesc(user, "FORUM")
                .stream()
                .map(Bookmark::getContentId)
                .collect(Collectors.toList());
    }

    @Override
    public int countByUserAndType(User user, String type) {
        return bookmarkRepository.countByUserAndContentType(user, type);
    }

    private List<Long> getContentIds(User user, String type) {
        return bookmarkRepository
                .findByUserAndContentTypeOrderByCreatedAtDesc(user, type)
                .stream()
                .map(Bookmark::getContentId)
                .collect(Collectors.toList());
    }
}
