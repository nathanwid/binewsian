package com.binewsian.controller.admin;

import com.binewsian.annotation.RequireRole;
import com.binewsian.enums.Role;
import com.binewsian.model.Activity;
import com.binewsian.model.Category;
import com.binewsian.model.News;
import com.binewsian.model.User;
import com.binewsian.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@RequireRole(Role.ADMIN)
@RequiredArgsConstructor
public class AdminController {

    private final CategoryService categoryService;
    private final HomeService homeService;
    private final NewsService newsService;
    private final ActivityService activityService;
    private final ContributorService contributorService;
    private final ForumService forumService;
    private final CommentService commentService;

    @GetMapping("/panel")
    public String showAdminDashboardPage(
            @RequestParam(defaultValue = "0") int newsPage,
            @RequestParam(defaultValue = "0") int categoryPage,
            @RequestParam(defaultValue = "0") int activityPage,
            @RequestParam(defaultValue = "0") int contributorPage,
            @RequestParam(defaultValue = "0") int forumPage,
            @RequestParam(defaultValue = "0") int commentPage,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "news") String tab,
            HttpSession session,
            Model model
    ) {
        User user = (User) session.getAttribute("user");

        Page<News> news = newsService.findPaginated(newsPage, size);
        Page<Category> categories = categoryService.findPaginated(categoryPage, size);
        Page<Activity> activities = activityService.findPaginated(activityPage, size);
        Page<User> contributors = contributorService.findContributorPaginated(contributorPage, size);
        var forumReports = forumService.findReportedThreads(forumPage, size);
        var commentReports = commentService.findReportedComments(commentPage, size);

        model.addAttribute("summary", homeService.getAdminSummary());
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

        // Category
        model.addAttribute("categories", categories.getContent());
        model.addAttribute("categoryCurrentPage", categoryPage);
        model.addAttribute("categoryTotalPages", categories.getTotalPages());
        model.addAttribute("categoryTotalElements", categories.getTotalElements());
        model.addAttribute("categoryPageSize", categories.getSize());

        // Contributor
        model.addAttribute("contributors", contributors.getContent());
        model.addAttribute("contributorCurrentPage", contributorPage);
        model.addAttribute("contributorTotalPages", contributors.getTotalPages());
        model.addAttribute("contributorTotalElements", contributors.getTotalElements());
        model.addAttribute("contributorPageSize", contributors.getSize());

        // Forum Reports
        model.addAttribute("forumReports", forumReports.getContent());
        model.addAttribute("forumCurrentPage", forumReports.getNumber());
        model.addAttribute("forumTotalPages", forumReports.getTotalPages());
        model.addAttribute("forumTotalElements", forumReports.getTotalElements());
        model.addAttribute("forumPageSize", forumReports.getSize());

        // Comment Reports
        model.addAttribute("commentReports", commentReports.getContent());
        model.addAttribute("commentCurrentPage", commentReports.getNumber());
        model.addAttribute("commentTotalPages", commentReports.getTotalPages());
        model.addAttribute("commentTotalElements", commentReports.getTotalElements());
        model.addAttribute("commentPageSize", commentReports.getSize());

        // Active tab
        model.addAttribute("activeTab", tab);

        return "admin/panel";
    }

}
