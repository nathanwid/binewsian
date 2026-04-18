package com.binewsian.email;

public record EmailEvent(
        String to,
        String subject,
        String html
) {}