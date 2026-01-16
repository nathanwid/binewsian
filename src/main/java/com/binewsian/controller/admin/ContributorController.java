package com.binewsian.controller.admin;

import com.binewsian.annotation.RequireRole;
import com.binewsian.constant.AppConstant;
import com.binewsian.enums.Role;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.service.ContributorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller("adminContributorController")
@RequestMapping("/admin")
@RequireRole(Role.ADMIN)
@RequiredArgsConstructor
public class ContributorController {

    private final ContributorService contributorService;

    @PostMapping("/contributors/create")
    public ResponseEntity<?> createContributor(@RequestParam String username, @RequestParam String email) {
        try {
            contributorService.create(username, email);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }

    @PostMapping("/contributors/update")
    public ResponseEntity<?> updateContributor(@RequestParam Long id) {
        try {
            contributorService.update(id);
            return ResponseEntity.ok().build();
        } catch (BiNewsianException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(AppConstant.UNEXPECTED_SERVER_ERROR);
        }
    }

    @GetMapping("/contributors/search")
    @ResponseBody
    public List<Map<String, Object>> searchContributor(@RequestParam String query) {
        return contributorService.findAll().stream()
                .filter(c -> c.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                        c.getEmail().toLowerCase().contains(query.toLowerCase()))
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("username", c.getUsername());
                    map.put("email", c.getEmail());
                    return map;
                })
                .collect(Collectors.toList());
    }

}
