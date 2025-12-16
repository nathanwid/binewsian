package com.binewsian.controller;

import com.binewsian.constant.AppConstant;
import com.binewsian.dto.CreateActivityRequest;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Activity;
import com.binewsian.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping("/activities/create")
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
