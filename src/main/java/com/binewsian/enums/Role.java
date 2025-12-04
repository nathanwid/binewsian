package com.binewsian.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    USER("User"),
    CONTRIBUTOR("Contributor"),
    ADMIN("Admin");

    private final String displayName;
}
