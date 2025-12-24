package com.binewsian.dto;

import com.binewsian.enums.ActivityType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

public record ActivityRequest(
        String title,
        ActivityType activityType,
        Integer quota,
        Integer rewardAmount,
        String registrationLink,
        String location,

        @DateTimeFormat(pattern = "HH:mm")
        LocalTime timeStart,

        @DateTimeFormat(pattern = "HH:mm")
        LocalTime timeEnd,

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate activityDate,

        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        LocalDateTime registrationDeadline,

        String details,
        Boolean isDraft
) {
}
