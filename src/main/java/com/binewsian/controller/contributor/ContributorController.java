package com.binewsian.controller.contributor;

import com.binewsian.annotation.RequireRole;
import com.binewsian.enums.Role;
import com.binewsian.model.Activity;
import com.binewsian.model.News;
import com.binewsian.model.User;
import com.binewsian.service.ActivityService;
import com.binewsian.service.HomeService;
import com.binewsian.service.NewsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller("contributorController")
@RequestMapping("/contributor")
@RequireRole(Role.CONTRIBUTOR)
@RequiredArgsConstructor
public class ContributorController {

    private final HomeService homeService;
    private final NewsService newsService;
    private final ActivityService activityService;

    @GetMapping("/content")
    public String showContributorDashboardPage(
            @RequestParam(defaultValue = "0") int newsPage,
            @RequestParam(defaultValue = "0") int activityPage,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "news") String tab,
            HttpSession session,
            Model model
    ) {
        User user = (User) session.getAttribute("user");
        Long userId = user.getId();

        Page<News> news = newsService.findPaginatedByUserId(newsPage, size, userId);
        Page<Activity> activities = activityService.findPaginatedByUserId(activityPage, size, userId);

        model.addAttribute("summary", homeService.getContributorSummary(userId));
        model.addAttribute("user", user);

        // News
        model.addAttribute("news", news.getContent());
        model.addAttribute("newsCurrentPage", newsPage);
        model.addAttribute("newsTotalPages", news.getTotalPages());
        model.addAttribute("newsTotalElements", news.getTotalElements());
        model.addAttribute("newsPageSize", news.getSize());

        // Activity
        model.addAttribute("activities", activities.getContent());
        model.addAttribute("activitiesCurrentPage", activityPage);
        model.addAttribute("activitiesTotalPages", activities.getTotalPages());
        model.addAttribute("activitiesTotalElements", activities.getTotalElements());
        model.addAttribute("activitiesPageSize", activities.getSize());

        // Active tab
        model.addAttribute("activeTab", tab);

        return "contributor/content";
    }

}
