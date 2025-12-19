package com.binewsian.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NewsStatus {
    DRAFT("Draft"),
    PUBLISHED("Published");

    private final String displayName;
}
