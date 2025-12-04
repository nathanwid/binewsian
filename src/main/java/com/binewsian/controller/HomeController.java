package com.binewsian.controller;

import com.binewsian.annotation.RequireRole;
import com.binewsian.enums.Role;
import com.binewsian.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

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
    public String adminPanel(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        return "admin/panel";
    }
}
