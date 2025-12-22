package com.binewsian.service;

import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Category;

import java.util.List;

import org.springframework.data.domain.Page;

public interface CategoryService {
    void create(String name) throws BiNewsianException;
    void update(Long id, String name) throws BiNewsianException;
    void delete(Long id) throws BiNewsianException;
    List<Category> findAll();
    Page<Category> findPaginated(int page, int size);
}
