package com.binewsian.controller.user;

import com.binewsian.annotation.RequireRole;
import com.binewsian.constant.AppConstant;
import com.binewsian.enums.Role;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Activity;
import com.binewsian.model.News;
import com.binewsian.model.User;
import com.binewsian.service.BookmarkService;
import com.binewsian.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookmarkService bookmarkService;

    @GetMapping("/profile")
    @RequireRole({Role.USER, Role.CONTRIBUTOR, Role.ADMIN})
    public String showUserProfilePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/change-password")
    @RequireRole({Role.USER, Role.CONTRIBUTOR, Role.ADMIN})
    public ResponseEntity<?> changePassword(@RequestParam String oldPassword, @RequestParam String newPassword, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            userService.changePassword(user.getId(), oldPassword, newPassword);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }

    @PostMapping("/edit-profile")
    @RequireRole({Role.USER, Role.CONTRIBUTOR, Role.ADMIN})
    public ResponseEntity<?> editProfile(@RequestParam String username, @RequestParam String email, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            User updatedUser = userService.updateProfile(user.getId(), username, email);
            session.setAttribute("user", updatedUser);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }

    @GetMapping("/bookmarks")
    @RequireRole({Role.USER})
    public String showBookmarksPage(@RequestParam(required = false, defaultValue = "all") String tab, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        long activityCount = bookmarkService.countByUserAndType(user, "ACTIVITY");
        long newsCount = bookmarkService.countByUserAndType(user, "NEWS");
        long forumCount = bookmarkService.countByUserAndType(user, "FORUM");
        long totalCount = activityCount + newsCount + forumCount;

        model.addAttribute("user", user);
        model.addAttribute("activeTab", tab);
        model.addAttribute("activityCount", activityCount);
        model.addAttribute("newsCount", newsCount);
        model.addAttribute("forumCount", forumCount);
        model.addAttribute("totalCount", totalCount);

        if ("activity".equals(tab) || "all".equals(tab)) {
            List<Activity> activities = bookmarkService.getBookmarkedActivities(user);
            model.addAttribute("activities", activities);
        }

        if ("news".equals(tab) || "all".equals(tab)) {
            List<News> news = bookmarkService.getBookmarkedNews(user);
            model.addAttribute("news", news);
        }

//        if ("forums".equals(tab) || "all".equals(tab)) {}

        return "user/bookmarks";
    }

    @PostMapping("/bookmarks/toggle")
    @RequireRole({Role.USER})
    public ResponseEntity<?> toggleBookmark(@RequestParam String type, @RequestParam Long contentId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        boolean saved = bookmarkService.toggle(user, type, contentId);
        return ResponseEntity.ok(Map.of("saved", saved));
    }

}
