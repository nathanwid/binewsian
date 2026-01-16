package com.binewsian.controller.admin;

import com.binewsian.annotation.RequireRole;
import com.binewsian.constant.AppConstant;
import com.binewsian.enums.Role;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller("adminNewsController")
@RequestMapping("/admin")
@RequireRole(Role.ADMIN)
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @PostMapping("/news/delete")
    public ResponseEntity<?> deleteNews(@RequestParam Long id) {
        try {
            newsService.delete(id);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }

    @GetMapping("/news/search")
    @ResponseBody
    public List<Map<String, Object>> searchNews(@RequestParam String query) {
        return newsService.findAllByStatus().stream()
                .filter(n -> n.getTitle().toLowerCase().contains(query.toLowerCase()))
                .map(n -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", n.getId());
                    map.put("title", n.getTitle());
                    map.put("status", n.getStatus().getDisplayName());
                    map.put("category", n.getCategory().getName());
                    map.put("featuredImageUrl", n.getFeaturedImageUrl());
                    map.put("publishedAt", n.getPublishedAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
                    return map;
                })
                .collect(Collectors.toList());
    }

}
