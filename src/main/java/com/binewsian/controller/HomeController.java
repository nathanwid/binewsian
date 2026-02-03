package com.binewsian.controller;

import com.binewsian.annotation.RequireRole;
import com.binewsian.dto.ActivityFilterDto;
import com.binewsian.dto.NewsFilterDto;
import com.binewsian.enums.ActivityType;
import com.binewsian.enums.Role;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Activity;
import com.binewsian.model.Category;
import com.binewsian.service.ActivityService;
import com.binewsian.service.BookmarkService;
import com.binewsian.model.News;
import com.binewsian.model.User;
import com.binewsian.service.CategoryService;
import com.binewsian.service.NewsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ActivityService activityService;
    private final BookmarkService bookmarkService;
    private final CategoryService categoryService;
    private final NewsService newsService;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    @RequireRole({Role.USER, Role.CONTRIBUTOR, Role.ADMIN})
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        List<News> news = newsService.findLatestPublished();

        model.addAttribute("user", user);
        model.addAttribute("news",  news);

        return "dashboard";
    }

    @GetMapping("/activity")
    @RequireRole({Role.USER, Role.CONTRIBUTOR, Role.ADMIN})
    public String showActivityPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String sort,
            HttpSession session,
            Model model
    ) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);

        ActivityFilterDto filterDto = new ActivityFilterDto();
        filterDto.setStatus(status != null ? status : "all");
        filterDto.setSort(sort != null ? sort : "newest");

        if (search != null && !search.trim().isEmpty()) {
            filterDto.setSearch(search.trim());
        }

        if (location != null && !location.isEmpty()) {
            filterDto.setLocationType(Arrays.asList(location.split(",")));
        }

        if (type != null && !type.isEmpty()) {
            List<ActivityType> activityTypes = Arrays.stream(type.split(","))
                    .map(ActivityType::valueOf)
                    .collect(Collectors.toList());

            filterDto.setType(activityTypes);
        }

        filterDto.setDateFrom(from);
        filterDto.setDateTo(to);

        Page<Activity> activityPage = activityService.getFilteredActivities(filterDto, page, size);

        model.addAttribute("activities", activityPage.getContent());
        model.addAttribute("currentPage", activityPage.getNumber());
        model.addAttribute("totalPages", activityPage.getTotalPages());
        model.addAttribute("totalActivities", activityPage.getTotalElements());

        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("location", location);
        model.addAttribute("type", type);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("sort", sort);

        return "activity";
    }

    @GetMapping("/activity/{id}")
    @RequireRole({Role.USER, Role.CONTRIBUTOR, Role.ADMIN})
    public String showActivityDetail(@PathVariable Long id, HttpSession session, Model model) throws BiNewsianException {
        User user = (User) session.getAttribute("user");
        Activity activity = activityService.getActivityById(id);

        model.addAttribute("user", user);
        model.addAttribute("activity", activity);
        model.addAttribute("isBookmarked", bookmarkService.isBookmarked(user, "ACTIVITY", id));

        return "activity-detail";
    }  

    @GetMapping("/news")
    @RequireRole({Role.USER, Role.CONTRIBUTOR, Role.ADMIN})
    public String showNewsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) String category,
            HttpSession session,
            Model model
    ) {
        User user = (User) session.getAttribute("user");
        List<Category> categories = categoryService.findAll();

        model.addAttribute("user", user);
        model.addAttribute("categories", categories);

        NewsFilterDto filterDto = new NewsFilterDto();

        if (category != null && !category.isEmpty()) {
            filterDto.setCategory(category);
        }

        Page<News> newsPage = newsService.getFilteredNews(filterDto, page, size);

        model.addAttribute("news", newsPage.getContent());
        model.addAttribute("currentPage", newsPage.getNumber());
        model.addAttribute("totalPages", newsPage.getTotalPages());
        model.addAttribute("totalNews", newsPage.getTotalElements());

        model.addAttribute("category", category);

        return "news";
    }
      
    @GetMapping("/news/{id}")
    @RequireRole({Role.USER, Role.CONTRIBUTOR, Role.ADMIN})
    public String showNewsDetailPage(@PathVariable Long id, HttpSession session, Model model) {
        try {
            User user = (User) session.getAttribute("user");
            News news = newsService.findById(id);

            model.addAttribute("user", user);
            model.addAttribute("news",  news);
            model.addAttribute("isBookmarked", bookmarkService.isBookmarked(user, "NEWS", id));

            return "news-detail";
        } catch (BiNewsianException e) {
            return "redirect:/dashboard";
        }
    }

}
