package com.binewsian.controller.user;

import com.binewsian.dto.ForumVoteResponse;
import com.binewsian.enums.VoteType;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.ForumThread;
import com.binewsian.model.User;
import com.binewsian.service.BookmarkService;
import com.binewsian.service.ForumService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@AllArgsConstructor
@RequestMapping("/forum")
public class ForumController {

    private final ForumService forumService;
    private final BookmarkService bookmarkService;

    @GetMapping
    public String forumPage(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "6") int size,
                            @RequestParam(required = false) String search,
                            Model model,
                            HttpSession session) {
        User user = (User) session.getAttribute("user");

        var threadPage = forumService.findPaginated(page, size, search);
        List<ForumThread> threads = threadPage.getContent();
        List<Long> threadIds = threads.stream().map(ForumThread::getId).toList();

        Map<Long, Long> replyCounts = forumService.countRepliesByThreadIds(threadIds);
        Map<Long, Long> upvoteCounts = forumService.countVotesByThreadIds(threadIds, VoteType.UP);
        Map<Long, Long> downvoteCounts = forumService.countVotesByThreadIds(threadIds, VoteType.DOWN);
        Map<Long, VoteType> userVotes = forumService.getUserVotesByThreadIds(user, threadIds);
        Map<Long, Long> reportCounts = forumService.countReportsByThreadIds(threadIds);

        Set<Long> savedThreadIds = new HashSet<>(bookmarkService.getContentIds(user, "THREAD"));

        model.addAttribute("threads", threads);
        model.addAttribute("replyCounts", replyCounts);
        model.addAttribute("upvoteCounts", upvoteCounts);
        model.addAttribute("downvoteCounts", downvoteCounts);
        model.addAttribute("userVotes", userVotes);
        model.addAttribute("reportCounts", reportCounts);
        model.addAttribute("savedThreadIds", savedThreadIds);
        model.addAttribute("currentPage", threadPage.getNumber());
        model.addAttribute("totalPages", threadPage.getTotalPages());
        model.addAttribute("totalThreads", threadPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("user", user);
        model.addAttribute("search", search);
        model.addAttribute("popularThreads", forumService.getPopularThreads(3));
        model.addAttribute("mostCommentedThreads", forumService.getMostCommentedThreads(3));
        model.addAttribute("mostLikedThreads", forumService.getMostLikedThreads(3));
        return "user/forum/list";
    }

    @PostMapping
    public String createThread(@RequestParam("title") String title,
                               @RequestParam("content") String content,
                               HttpSession session) {

        User user = (User) session.getAttribute("user");

        try {
            forumService.createThread(title, content, user);
            return "redirect:/forum";
        } catch (BiNewsianException ex) {
            // kirim error message ke query param biar gampang debug
            return "redirect:/forum?error=" + ex.getMessage();
        }
    }

    @GetMapping("/{id}")
    public String threadDetail(@PathVariable("id") Long threadId,
                               Model model,
                               HttpSession session) {

        User user = (User) session.getAttribute("user");

        try {
            model.addAttribute("thread", forumService.getThreadById(threadId));
            model.addAttribute("upvoteCount", forumService.countVotes(threadId, VoteType.UP));
            model.addAttribute("downvoteCount", forumService.countVotes(threadId, VoteType.DOWN));
            model.addAttribute("userVote", forumService.getUserVote(threadId, user));
            model.addAttribute("isSaved", bookmarkService.isBookmarked(user, "THREAD", threadId));
            model.addAttribute("reportCount", forumService.countReports(threadId));
            model.addAttribute("hasReported", forumService.hasReported(threadId, user));
            model.addAttribute("user", user);
            return "user/forum/detail";
        } catch (BiNewsianException ex) {
            return "redirect:/forum?error=" + ex.getMessage();
        }
    }

    @PostMapping("/{id}/vote")
    @ResponseBody
    public ResponseEntity<?> voteThread(@PathVariable("id") Long threadId,
                                        @RequestParam("type") String type,
                                        HttpSession session) {
        User user = (User) session.getAttribute("user");

        try {
            VoteType voteType = VoteType.valueOf(type);
            ForumVoteResponse response = forumService.voteThread(threadId, user, voteType);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Invalid vote type");
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteThread(@PathVariable("id") Long threadId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        try {
            forumService.deleteThread(threadId, user);
            return "redirect:/forum";
        } catch (BiNewsianException ex) {
            return "redirect:/forum?error=" + ex.getMessage();
        }
    }

    @PostMapping("/{id}/report")
    @ResponseBody
    public ResponseEntity<?> reportThread(@PathVariable("id") Long threadId,
                                          @RequestParam(required = false) String reason,
                                          HttpSession session) {
        User user = (User) session.getAttribute("user");
        try {
            forumService.reportThread(threadId, user, reason);
            long reportCount = forumService.countReports(threadId);
            return ResponseEntity.ok(Map.of("reportCount", reportCount));
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Unexpected server error.");
        }
    }
}
