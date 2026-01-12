package com.binewsian.controller;

import com.binewsian.constant.AppConstant;
import com.binewsian.dto.CommentRequest;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Comment;
import com.binewsian.model.User;
import com.binewsian.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

    @GetMapping("/comments")
    @ResponseBody
    public ResponseEntity<?> getComments(
            @RequestParam String contentType,
            @RequestParam Long contentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Page<Comment> comments = commentService.findPaginated(page, size, contentId, contentType);

        Map<String, Object> response = new HashMap<>();
        response.put("comments", comments.getContent());
        response.put("currentPage", comments.getNumber());
        response.put("totalItems", comments.getTotalElements());
        response.put("totalPages", comments.getTotalPages());

        return ResponseEntity.ok(response);
    }
}
