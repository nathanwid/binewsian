package com.binewsian.controller;

import com.binewsian.annotation.RequireRole;
import com.binewsian.enums.Role;
import com.binewsian.model.Activity;
import com.binewsian.model.Category;
import com.binewsian.model.News;
import com.binewsian.model.User;
import com.binewsian.service.ActivityService;
import com.binewsian.service.CategoryService;
import com.binewsian.service.HomeService;
import com.binewsian.service.NewsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CategoryService categoryService;
    private final HomeService homeService;
    private final NewsService newsService;
    private final ActivityService activityService;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    @RequireRole({Role.USER, Role.CONTRIBUTOR, Role.ADMIN})
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        return "dashboard";
    }

    @GetMapping("/user/profile")
    @RequireRole({Role.USER, Role.CONTRIBUTOR, Role.ADMIN})
    public String userProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        return "user/profile";
    }

    @GetMapping("/contributor/content")
    @RequireRole(Role.CONTRIBUTOR)
    public String contributorContent(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        return "contributor/content";
    }

    @GetMapping("/admin/panel")
    @RequireRole(Role.ADMIN)
    public String adminPanel(
            @RequestParam(defaultValue = "0") int newsPage,
            @RequestParam(defaultValue = "0") int categoryPage,
            @RequestParam(defaultValue = "0") int activityPage,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "news") String tab,
            HttpSession session,
            Model model
    ) {
        User user = (User) session.getAttribute("user");

        Page<News> news = newsService.findPaginated(newsPage, size);
        Page<Category> categories = categoryService.findPaginated(categoryPage, size);
        Page<Activity> activities = activityService.findPaginated(activityPage, size);

        model.addAttribute("summary", homeService.getAdminSummary());
        model.addAttribute("user", user);

        // News
        model.addAttribute("news", news.getContent());
        model.addAttribute("newsCurrentPage", newsPage);
        model.addAttribute("newsTotalPages", news.getTotalPages());

        // Activity
        model.addAttribute("activities", activities.getContent());
        model.addAttribute("activitiesCurrentPage", activityPage);
        model.addAttribute("activitiesTotalPages", activities.getTotalPages());

        // Category
        model.addAttribute("categories", categories.getContent());
        model.addAttribute("categoryCurrentPage", categoryPage);
        model.addAttribute("categoryTotalPages", categories.getTotalPages());

        // Active tab
        model.addAttribute("activeTab", tab);

        return "admin/panel";
    }

}
