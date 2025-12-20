package com.binewsian.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NewsCategory {
    TECHNOLOGY("Technology"),
    BUSINESS("Business"),
    SPORTS("Sports"),
    ENTERTAINMENT("Entertainment"),
    HEALTH("Health"),
    SCIENCE("Science");

    private final String displayName;
}
