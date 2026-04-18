package com.binewsian.service.impl;

import com.binewsian.constant.AppConstant;
import com.binewsian.dto.ActivityFilterDto;
import com.binewsian.dto.ActivityRequest;
import com.binewsian.enums.ActivityStatus;
import com.binewsian.enums.ActivityType;
import com.binewsian.enums.Role;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Activity;
import com.binewsian.model.User;
import com.binewsian.repository.ActivityRepository;
import com.binewsian.repository.UserRepository;
import com.binewsian.service.ActivityService;
import com.binewsian.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public void create(ActivityRequest request, User user, String appUrl) throws BiNewsianException {
        boolean isDraft = request.isDraft();

        if (isDraft) {
            validateDraft(request);
        } else {
            validatePublish(request);
        }

        Activity activity = new Activity();
        activity.setTitle(request.title());
        activity.setType(request.activityType());
        activity.setQuota(request.quota());
        activity.setRewardAmount(request.rewardAmount());
        activity.setRegistrationLink(request.registrationLink());
        activity.setLocation(request.location());
        activity.setTimeStart(request.timeStart());
        activity.setTimeEnd(request.timeEnd());
        activity.setActivityDate(request.activityDate());
        activity.setRegistrationDeadline(request.registrationDeadline());
        activity.setDetails(request.details());
        activity.setStatus(isDraft ? ActivityStatus.DRAFT : ActivityStatus.PUBLISHED);
        activity.setPublishedAt(isDraft ? null : LocalDateTime.now());
        activity.setCreatedBy(user);

        Activity savedActivity = activityRepository.save(activity);

        if (!isDraft) {
            notifyUsers(savedActivity, appUrl);
        }
    }

    @Override
    public void update(Long id, ActivityRequest request, User user, String appUrl) throws BiNewsianException {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new BiNewsianException(AppConstant.ACTIVITY_NOT_FOUND));

        if (!activity.getCreatedBy().getId().equals(user.getId())) {
            throw new BiNewsianException("You are not authorized to edit this activity.");
        }

        boolean isDraft = request.isDraft();

        if (isDraft) {
            validateDraft(request);
        } else {
            validatePublish(request);
        }

        activity.setTitle(request.title());
        activity.setType(request.activityType());
        activity.setQuota(request.quota());
        activity.setRewardAmount(request.rewardAmount());
        activity.setRegistrationLink(request.registrationLink());
        activity.setLocation(request.location());
        activity.setTimeStart(request.timeStart());
        activity.setTimeEnd(request.timeEnd());
        activity.setActivityDate(request.activityDate());
        activity.setRegistrationDeadline(request.registrationDeadline());
        activity.setDetails(request.details());
        activity.setStatus(isDraft ? ActivityStatus.DRAFT : ActivityStatus.PUBLISHED);
        activity.setPublishedAt(isDraft ? null : LocalDateTime.now());

        Activity savedActivity = activityRepository.save(activity);

        if (!isDraft) {
            notifyUsers(savedActivity, appUrl);
        }
    }

    @Override
    public void delete(Long id) throws BiNewsianException {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new BiNewsianException(AppConstant.ACTIVITY_NOT_FOUND));

        activityRepository.delete(activity);
    }

    @Override
    public Activity findById(Long id) throws BiNewsianException {
        return activityRepository.findById(id).orElse(null);
    }

    @Override
    public Activity findClosestBookmarkedActivity(Long userId) {
        return activityRepository.findClosestBookmarkedActivity(userId).orElse(null);
    }

    @Override
    public Page<Activity> findPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return activityRepository.findByStatus(ActivityStatus.PUBLISHED, pageable);
    }

    @Override
    public Page<Activity> findPaginatedByUserId(int page, int size, Long userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return activityRepository.findByCreatedBy_Id(userId, pageable);
    }

    @Override
    public List<Activity> findAllByStatus() {
        return activityRepository.findByStatusOrderByPublishedAtDesc(ActivityStatus.PUBLISHED);
    }

    @Override
    public List<Activity> findAllByUserId(Long userId) {
        return activityRepository.findByCreatedBy_IdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Page<Activity> getFilteredActivities(ActivityFilterDto filterDto, int page, int size) {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime registrationDeadlineStart = null;
        LocalDateTime registrationDeadlineEnd = null;

        if (filterDto.getStatus() != null) {
            if (filterDto.getStatus().equals("available")) {
                registrationDeadlineStart = now;
            } else if (filterDto.getStatus().equals("closed")) {
                registrationDeadlineEnd = now;
            }
        }

        boolean includeOnline;
        boolean includeOnsite;

        if (filterDto.getLocationType() != null && !filterDto.getLocationType().isEmpty()) {
            includeOnline = filterDto.getLocationType().contains("online");
            includeOnsite = filterDto.getLocationType().contains("onsite");
        } else {
            includeOnline = true;
            includeOnsite = true;
        }

        boolean hasTypeFilter = filterDto.getType() != null && !filterDto.getType().isEmpty();
        List<ActivityType> types = hasTypeFilter ? filterDto.getType() : List.of(ActivityType.STUDENT_ACTIVITY_TRANSCRIPT);

        String searchTerm = null;
        if (filterDto.getSearch() != null && !filterDto.getSearch().trim().isEmpty()) {
            searchTerm = "%" + filterDto.getSearch().trim().toLowerCase() + "%";
        }

        Sort sort = getSort(filterDto.getSort());
        Pageable pageable = PageRequest.of(page, size, sort);

        return activityRepository.findActivitiesWithFilters(
                ActivityStatus.PUBLISHED,
                registrationDeadlineStart,
                registrationDeadlineEnd,
                includeOnline,
                includeOnsite,
                hasTypeFilter,
                types,
                filterDto.getDateFrom(),
                filterDto.getDateTo(),
                searchTerm,  // Add search parameter
                pageable
        );
    }

    private void notifyUsers(Activity activity, String appUrl) {
        List<User> users = userRepository.findByRoleAndEnabledTrue(Role.USER);

        Map<String, Object> data = new HashMap<>();
        data.put("contentType", "ACTIVITY");
        data.put("activityDate", activity.getActivityDate());
        data.put("author", activity.getCreatedBy().getUsername());
        data.put("contentTitle", activity.getTitle());
        data.put("contentDescription", activity.getDetails());
        data.put("contentUrl", appUrl + "/activity/" + activity.getId());

        emailService.sendContentNotification(users, data);
    }

    private Sort getSort(String sortBy) {
        if (sortBy == null || sortBy.equals("newest")) {
            return Sort.by(Sort.Direction.DESC, "activityDate");
        } else if (sortBy.equals("oldest")) {
            return Sort.by(Sort.Direction.ASC, "activityDate");
        } else if (sortBy.equals("reward")) {
            return Sort.by(Sort.Direction.DESC, "rewardAmount");
        }
        return Sort.by(Sort.Direction.DESC, "activityDate");
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) return url;

        url = url.trim();

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }

        return url;
    }

    private void validateDraft(ActivityRequest r) throws BiNewsianException {
        validateBaseDateTime(r);
    }

    private void validateBaseDateTime(ActivityRequest r) throws BiNewsianException {
        LocalDate activityDate = r.activityDate();
        LocalTime timeStart = r.timeStart();
        LocalTime timeEnd = r.timeEnd();
        LocalDateTime registrationDeadline = r.registrationDeadline();
        
        if (activityDate == null && timeStart == null && timeEnd == null && registrationDeadline == null) {
            return;
        }

        if (timeStart != null && timeEnd != null && !timeEnd.isAfter(timeStart)) {
            throw new BiNewsianException("End time must be after start time");
        }

        LocalDateTime activityStartDateTime = activityDate.atTime(timeStart);

        if (registrationDeadline != null && !registrationDeadline.isBefore(activityStartDateTime)) {
            throw new BiNewsianException("Registration deadline must be before activity start time");
        }

        if (activityDate != null && activityDate.isBefore(LocalDate.now())) {
            throw new BiNewsianException("Activity date cannot be in the past");
        }
    }

    private void validatePublishDateTime(ActivityRequest r) throws BiNewsianException {
        if (r.activityDate() == null) {
            throw new BiNewsianException("Activity date is required");
        }

        if (r.timeStart() == null || r.timeEnd() == null) {
            throw new BiNewsianException("Start time and end time are required");
        }

        if (r.registrationDeadline() == null) {
            throw new BiNewsianException("Registration deadline is required");
        }
        
        validateBaseDateTime(r);
    }

    private void validatePublish(ActivityRequest r) throws BiNewsianException {
        String title = r.title();

        if (title == null || title.isBlank()) {
            throw new BiNewsianException("Title is required");
        }

        if (r.activityType() == null) {
            throw new BiNewsianException("Activity type is required");
        }

        String location = r.location();

        if (location == null || location.isBlank()) {
            throw new BiNewsianException("Location is required");
        }

        String details = r.details();

        if (details == null || details.isBlank()) {
            throw new BiNewsianException("Activity content cannot be empty");
        }

        Integer quota = r.quota();

        if (quota == null || quota <= 0) {
            throw new BiNewsianException("Quota must be greater than 0");
        }

        Integer rewardAmount = r.rewardAmount();

        if (rewardAmount == null || rewardAmount <= 0) {
            throw new BiNewsianException("Reward amount must be greater than 0");
        }

        validatePublishDateTime(r);

        String registrationLink = r.registrationLink();
        
        if (registrationLink == null || registrationLink.isBlank()) {
            throw new BiNewsianException("Registration link is required");
        }

        try {
            new URI(normalizeUrl(registrationLink));
        } catch (Exception e) {
            throw new BiNewsianException("Registration link is not a valid URL");
        }
    }
}
