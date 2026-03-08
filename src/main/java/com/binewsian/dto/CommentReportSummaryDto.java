package com.binewsian.dto;

import com.binewsian.model.Comment;

import java.time.LocalDateTime;

public record CommentReportSummaryDto(Comment comment, long reportCount, String latestReason, LocalDateTime latestReportedAt) {
}
