package com.binewsian.dto;

public record CreateNewsRequest(
        String title,
        Long categoryId,
        String summary,
        String content,
        Boolean isDraft
) {
}
