package com.binewsian.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActivityType {
    SAT("Student Activity Transcript"),
    COMMUNITY_SERVICE("Community Service");

    private final String displayName;
}
