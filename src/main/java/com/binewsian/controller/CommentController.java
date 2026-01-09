package com.binewsian.controller;

import com.binewsian.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    
}
