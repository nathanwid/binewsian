package com.binewsian.controller.contributor;

import com.binewsian.annotation.RequireRole;
import com.binewsian.enums.Role;
import com.binewsian.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/contributor")
@RequireRole(Role.CONTRIBUTOR)
public class ActivityController {

    @GetMapping("/create-activity")
    public String showCreateActivityPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        return "contributor/create-activity";
    }

}
