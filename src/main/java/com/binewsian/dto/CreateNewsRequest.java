package com.binewsian.dto;

import com.binewsian.enums.NewsCategory;

public record CreateNewsRequest(
        String title,
        NewsCategory category,
        String summary,
        String content,
        Boolean isDraft
) {
}
