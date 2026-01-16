package com.binewsian.dto;

public record CommentRequest(
        String content,
        Long contentId,
        String contentType,
        Long parentId
) {
}
