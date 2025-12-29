package com.binewsian.dto;

public record NewsRequest(
        String title,
        Long categoryId,
        String summary,
        String content,
        boolean isDraft,
        boolean deleteImage
) {
}
