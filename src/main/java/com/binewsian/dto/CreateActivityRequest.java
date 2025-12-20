package com.binewsian.dto;

import com.binewsian.enums.ActivityType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record CreateActivityRequest(
        String title,
        ActivityType activityType,
        Integer quota,
        Integer rewardAmount,
        String registrationLink,
        String location,
        LocalTime timeStart,
        LocalTime timeEnd,
        LocalDate activityDate,
        LocalDateTime registrationDeadline,
        String details,
        Boolean isDraft
) {
}
