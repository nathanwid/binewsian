package com.binewsian.service;

import com.binewsian.model.User;

public interface BookmarkService {
    boolean toggle(User user, String type, Long contentId);
    boolean isBookmarked(User user, String type, Long contentId);
}
