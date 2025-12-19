package com.binewsian.controller.contributor;

import com.binewsian.annotation.RequireRole;
import com.binewsian.constant.AppConstant;
import com.binewsian.dto.CreateActivityRequest;
import com.binewsian.enums.Role;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.User;
import com.binewsian.service.ActivityService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/contributor")
@RequireRole(Role.CONTRIBUTOR)
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping("/create-activity")
    public String showCreateActivityPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        return "contributor/create-activity";
    }

    @PostMapping("/activities")
    public ResponseEntity<?> createActivity(@RequestBody CreateActivityRequest request) {
        try {
            activityService.create(request);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }

}
