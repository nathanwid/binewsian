package com.binewsian.controller.contributor;

import com.binewsian.annotation.RequireRole;
import com.binewsian.constant.AppConstant;
import com.binewsian.dto.NewsRequest;
import com.binewsian.enums.NewsStatus;
import com.binewsian.enums.Role;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Category;
import com.binewsian.model.News;
import com.binewsian.model.User;
import com.binewsian.service.CategoryService;
import com.binewsian.service.NewsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller("contributorNewsController")
@RequestMapping("/contributor")
@RequireRole(Role.CONTRIBUTOR)
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;
    private final CategoryService categoryService;

    @GetMapping("/create-news")
    public String showCreateNewsPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        List<Category> categories = categoryService.findAll();

        model.addAttribute("user", user);
        model.addAttribute("categories", categories);

        return "contributor/create-news";
    }

    @GetMapping("/news/search")
    @ResponseBody
    public List<Map<String, Object>> searchNews(@RequestParam String query, HttpSession session) {
        User user = (User) session.getAttribute("user");
        return newsService.findAllByUserId(user.getId()).stream()
                .filter(n -> n.getTitle().toLowerCase().contains(query.toLowerCase()))
                .map(n -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", n.getId());
                    map.put("title", n.getTitle());
                    map.put("statusName", n.getStatus() != null ? n.getStatus().getDisplayName() : "-");
                    map.put("statusClass", n.getStatus() != null ? n.getStatus().getCssClass() : "");
                    map.put("category", n.getCategory() != null ? n.getCategory().getName() : "-");
                    map.put("featuredImageUrl", n.getFeaturedImageUrl());
                    map.put("publishedAt",
                            n.getPublishedAt() != null
                                    ? n.getPublishedAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                                    : null);
                    return map;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/edit-news/{id}")
    public String showEditNewsPage(@PathVariable Long id, HttpSession session, Model model) {
        try {
            User user = (User) session.getAttribute("user");
            News news = newsService.findById(id);
            List<Category> categories = categoryService.findAll();
            boolean canEdit = news.getCreatedBy().getId().equals(user.getId());

            model.addAttribute("user", user);
            model.addAttribute("news", news);
            model.addAttribute("categories", categories);
            model.addAttribute("canEdit", canEdit);
            
            return "contributor/edit-news";
        } catch (BiNewsianException e) {
            return "redirect:/contributor/content";
        }
    }

    @PostMapping(value = "/news", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createNews(
            @RequestPart("data") NewsRequest data,
            @RequestPart(value = "featuredImage", required = false) MultipartFile featuredImage,
            HttpSession session,
            HttpServletRequest request
    ) {
        String appUrl = request.getScheme() + "://" + request.getServerName() +
                ":" + request.getServerPort() + request.getContextPath();

        try {
            User user = (User) session.getAttribute("user");
            newsService.create(data, featuredImage, user, appUrl);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }

    @PutMapping(value = "/news/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateNews(
            @PathVariable Long id,
            @RequestPart("data") NewsRequest data,
            @RequestPart(value = "featuredImage", required = false) MultipartFile featuredImage,
            HttpSession session,
            HttpServletRequest request
    ) {
        String appUrl = request.getScheme() + "://" + request.getServerName() +
                ":" + request.getServerPort() + request.getContextPath();

        try {
            User user = (User) session.getAttribute("user");
            newsService.update(id, data, featuredImage, user, appUrl);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }

    @DeleteMapping("/news/{id}")
    public ResponseEntity<?> deleteNews(@PathVariable Long id) {
        try {
            newsService.delete(id);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }
}
