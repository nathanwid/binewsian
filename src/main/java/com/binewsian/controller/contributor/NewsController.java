package com.binewsian.controller.contributor;

import com.binewsian.annotation.RequireRole;
import com.binewsian.constant.AppConstant;
import com.binewsian.dto.CreateNewsRequest;
import com.binewsian.enums.Role;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.User;
import com.binewsian.service.NewsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/contributor")
@RequireRole(Role.CONTRIBUTOR)
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/create-news")
    public String showCreateNewsPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        return "contributor/create-news";
    }

    @PostMapping(value = "/news/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createNews(
            @RequestPart("data") CreateNewsRequest request,
            @RequestPart(value = "featuredImage", required = false) MultipartFile featuredImage
    ) {
        try {
            newsService.create(request, featuredImage);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }
}
