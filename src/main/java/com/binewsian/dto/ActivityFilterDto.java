package com.binewsian.dto;

import com.binewsian.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityFilterDto {
    private String status;
    private List<String> locationType;
    private List<ActivityType> type;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String sort;
}
