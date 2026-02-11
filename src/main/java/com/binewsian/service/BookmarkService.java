package com.binewsian.service;

import com.binewsian.model.Activity;
import com.binewsian.model.ForumThread;
import com.binewsian.model.News;
import com.binewsian.model.User;

import java.util.List;

public interface BookmarkService {
    boolean toggle(User user, String type, Long contentId);
    boolean isBookmarked(User user, String type, Long contentId);
    List<Activity> getBookmarkedActivities(User user);
    List<News> getBookmarkedNews(User user);
    List<ForumThread> getBookmarkedThreads(User user);
    int countByUserAndType(User user, String type);
}
