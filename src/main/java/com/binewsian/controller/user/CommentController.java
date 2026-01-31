package com.binewsian.controller.user;

import com.binewsian.constant.AppConstant;
import com.binewsian.dto.CommentRequest;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Comment;
import com.binewsian.model.User;
import com.binewsian.service.CommentService;
import com.binewsian.util.UserAvatar;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserAvatar userAvatar;

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

        List<Map<String, Object>> mappedComments = comments.getContent().stream()
                .map(c -> {
                    Map<String, Object> commentMap = new HashMap<>();

                    commentMap.put("id", c.getId());
                    commentMap.put("userId", c.getUser().getId());
                    commentMap.put("username", c.getUser().getUsername());
                    commentMap.put("contentId", c.getContentId());
                    commentMap.put("contentType", c.getContentType());
                    commentMap.put("content", c.getContent());
                    commentMap.put("replyCount", c.getReplyCount());
                    commentMap.put("deleted", c.getDeleted());
                    commentMap.put("deletedAt", c.getDeletedAt());
                    commentMap.put("updatedAt", c.getUpdatedAt());
                    commentMap.put("createdAt", c.getCreatedAt());

                    commentMap.put("avatarInitials", userAvatar.getUserInitials(c.getUser().getUsername()));
                    commentMap.put("avatarColor", userAvatar.getAvatarColor(c.getUser().getUsername()));

                    return commentMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("comments", mappedComments);
        response.put("currentPage", comments.getNumber());
        response.put("totalItems", comments.getTotalElements());
        response.put("totalPages", comments.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/comments/{id}/replies")
    @ResponseBody
    public ResponseEntity<?> getReplies(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Page<Comment> replies = commentService.findReplies(page, size, id);

        List<Map<String, Object>> mappedReplies = replies.getContent().stream()
                .map(r -> {
                    Map<String, Object> replyMap = new HashMap<>();

                    replyMap.put("id", r.getId());
                    replyMap.put("parentId", r.getParent().getId());
                    replyMap.put("userId", r.getUser().getId());
                    replyMap.put("username", r.getUser().getUsername());
                    replyMap.put("contentId", r.getContentId());
                    replyMap.put("contentType", r.getContentType());
                    replyMap.put("content", r.getContent());
                    replyMap.put("replyCount", r.getReplyCount());
                    replyMap.put("deleted", r.getDeleted());
                    replyMap.put("deletedAt", r.getDeletedAt());
                    replyMap.put("updatedAt", r.getUpdatedAt());
                    replyMap.put("createdAt", r.getCreatedAt());

                    replyMap.put("avatarInitials", userAvatar.getUserInitials(r.getUser().getUsername()));
                    replyMap.put("avatarColor", userAvatar.getAvatarColor(r.getUser().getUsername()));

                    return replyMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("replies", mappedReplies);
        response.put("currentPage", replies.getNumber());
        response.put("totalItems", replies.getTotalElements());
        response.put("totalPages", replies.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long id, 
            @RequestBody CommentRequest request, 
            HttpSession session
    ) {
        try {
            User user = (User) session.getAttribute("user");
            commentService.update(id, request, user);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            commentService.delete(id, user);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }
}
