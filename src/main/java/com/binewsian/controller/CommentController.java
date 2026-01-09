package com.binewsian.controller;

import com.binewsian.constant.AppConstant;
import com.binewsian.dto.CommentRequest;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.User;
import com.binewsian.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/comments")
    public ResponseEntity<?> createComment(@RequestBody CommentRequest request, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            commentService.create(request, user);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }
}
