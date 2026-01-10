package com.binewsian.service.impl;

import com.binewsian.model.Bookmark;
import com.binewsian.model.User;
import com.binewsian.repository.BookmarkRepository;
import com.binewsian.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    @Override
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
}
