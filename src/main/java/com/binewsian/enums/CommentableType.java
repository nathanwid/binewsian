package com.binewsian.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommentableType {
    NEWS("News"),
    ACTIVITY("Activity"),
    FORUM("Forum");

    private final String displayName;
}
