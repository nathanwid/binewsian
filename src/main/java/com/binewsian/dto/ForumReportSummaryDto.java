package com.binewsian.dto;

import com.binewsian.model.ForumThread;

import java.time.LocalDateTime;

public record ForumReportSummaryDto(ForumThread thread, long reportCount, String latestReason, LocalDateTime latestReportedAt) {
}
