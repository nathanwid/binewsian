package com.binewsian.dto;

import com.binewsian.enums.CommentableType;

public record CommentRequest(
        String content,
        Long commentableId,
        CommentableType commentableType
) {
}
