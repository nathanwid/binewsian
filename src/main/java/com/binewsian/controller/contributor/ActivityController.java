package com.binewsian.controller.contributor;

import com.binewsian.annotation.RequireRole;
import com.binewsian.constant.AppConstant;
import com.binewsian.dto.ActivityRequest;
import com.binewsian.enums.ActivityStatus;
import com.binewsian.enums.ActivityType;
import com.binewsian.enums.Role;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Activity;
import com.binewsian.model.User;
import com.binewsian.service.ActivityService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;


@Controller("contributorActivityController")
@RequestMapping("/contributor")
@RequireRole(Role.CONTRIBUTOR)
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping("/create-activity")
    public String showCreateActivityPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        ActivityType[] activityTypes = ActivityType.values();
        
        model.addAttribute("user", user);
        model.addAttribute("activityTypes", activityTypes);
        
        return "contributor/create-activity";
    }

    @GetMapping("/edit-activity/{id}")
    public String showEditActivityPage(@PathVariable Long id, HttpSession session, Model model) {
        try {
            User user = (User) session.getAttribute("user");
            Activity activity = activityService.findById(id);
            ActivityType[] activityTypes = ActivityType.values();
            boolean canEdit = activity.getStatus() == ActivityStatus.DRAFT && activity.getCreatedBy().getId().equals(user.getId());

            model.addAttribute("user", user);
            model.addAttribute("activity", activity);
            model.addAttribute("activityTypes", activityTypes);
            model.addAttribute("canEdit", canEdit);

            return "contributor/edit-activity";
        } catch (BiNewsianException e) {
            return "redirect:/contributor/content";
        }
    }

    @PostMapping("/activities")
    public ResponseEntity<?> createActivity(
            @RequestBody ActivityRequest request,
            HttpSession session
    ) {
        try {
            User user = (User) session.getAttribute("user");
            activityService.create(request, user);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }

    @PutMapping("/activities/{id}")
    public ResponseEntity<?> updateActivity(
            @PathVariable Long id, 
            @RequestBody ActivityRequest request,
            HttpSession session
    ) {
        try {
            User user = (User) session.getAttribute("user");
            activityService.update(id, request, user);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }

    @DeleteMapping("/activities/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) {
        try {
            activityService.delete(id);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }

}
