package com.binewsian.controller;

import com.binewsian.annotation.RequireRole;
import com.binewsian.enums.Role;
import com.binewsian.model.Category;
import com.binewsian.model.User;
import com.binewsian.service.CategoryService;
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
    public String adminPanel(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size,
                             HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        Page<Category> categoryPage = categoryService.findPaginated(0, size);

        model.addAttribute("user", user);
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        return "admin/panel";
    }
}
