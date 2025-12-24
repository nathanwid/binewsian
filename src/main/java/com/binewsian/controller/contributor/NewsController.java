package com.binewsian.controller.contributor;

import com.binewsian.annotation.RequireRole;
import com.binewsian.constant.AppConstant;
import com.binewsian.dto.CreateNewsRequest;
import com.binewsian.dto.UpdateNewsRequest;
import com.binewsian.enums.NewsStatus;
import com.binewsian.enums.Role;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Category;
import com.binewsian.model.News;
import com.binewsian.model.User;
import com.binewsian.service.CategoryService;
import com.binewsian.service.NewsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.List;

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

    @GetMapping("/edit-news/{id}")
    public String showEditNewsPage(@PathVariable Long id, HttpSession session, Model model) {
        try {
            User user = (User) session.getAttribute("user");
            News news = newsService.findById(id);
            List<Category> categories = categoryService.findAll();
            boolean canEdit = news.getStatus() == NewsStatus.DRAFT && news.getCreatedBy().getId().equals(user.getId());

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
            @RequestPart("data") CreateNewsRequest request,
            @RequestPart(value = "featuredImage", required = false) MultipartFile featuredImage,
            HttpSession session
    ) {
        try {
            User user = (User) session.getAttribute("user");
            newsService.create(request, featuredImage, user);
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
            @RequestPart("data") UpdateNewsRequest request,
            @RequestPart(value = "featuredImage", required = false) MultipartFile featuredImage,
            HttpSession session
    ) {
        try {
            User user = (User) session.getAttribute("user");
            newsService.update(id, request, featuredImage, user);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }

    @DeleteMapping("/news/{id}")
    public ResponseEntity<?> deketeNews(@PathVariable Long id) {
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
