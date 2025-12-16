package com.binewsian.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActivityStatus {
    DRAFT("Draft"),
    PUBLISHED("Published");

    private final String displayName;
}
