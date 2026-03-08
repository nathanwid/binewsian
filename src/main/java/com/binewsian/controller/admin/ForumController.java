package com.binewsian.controller.admin;

import com.binewsian.annotation.RequireRole;
import com.binewsian.constant.AppConstant;
import com.binewsian.enums.Role;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.User;
import com.binewsian.service.CommentService;
import com.binewsian.service.ForumService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller("adminForumController")
@RequestMapping("/admin")
@RequireRole(Role.ADMIN)
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;
    private final CommentService commentService;

    @PostMapping("/forum/delete")
    public ResponseEntity<?> deleteForumThread(@RequestParam Long id, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            forumService.deleteThread(id, user);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }

    @PostMapping("/forum/comments/delete")
    public ResponseEntity<?> deleteForumComment(@RequestParam Long id, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            commentService.deleteByAdmin(id, user);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }

    @PostMapping("/comments/delete")
    public ResponseEntity<?> deleteComment(@RequestParam Long id, HttpSession session) {
        return deleteForumComment(id, session);
    }
}
