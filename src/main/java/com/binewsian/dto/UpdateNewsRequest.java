package com.binewsian.dto;

public record UpdateNewsRequest(
        String title,
        Long categoryId,
        String summary,
        String content,
        Boolean deleteImage,
        Boolean isDraft
) {
}
