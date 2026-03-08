package com.binewsian.dto;

import com.binewsian.model.ForumThread;

public record ForumThreadPopularityDto(ForumThread thread, long commentCount, long upvoteCount, long score) {
}
