package com.binewsian.controller.admin;

import com.binewsian.annotation.RequireRole;
import com.binewsian.constant.AppConstant;
import com.binewsian.enums.Role;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller("adminActivityController")
@RequestMapping("/admin")
@RequireRole(Role.ADMIN)
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping("/activities/delete")
    public ResponseEntity<?> deleteActivity(@RequestParam Long id) {
        try {
            activityService.delete(id);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }

    @GetMapping("/activities/search")
    @ResponseBody
    public List<Map<String, Object>> searchActivity(@RequestParam String query) {
        return activityService.findAllByStatus().stream()
                .filter(a -> a.getTitle().toLowerCase().contains(query.toLowerCase()))
                .map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", a.getId());
                    map.put("title", a.getTitle());
                    map.put("type", a.getType().getDisplayName());
                    map.put("reward", a.getRewardAmount());
                    return map;
                })
                .collect(Collectors.toList());
    }

}
