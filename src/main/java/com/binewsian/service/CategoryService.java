package com.binewsian.service;

import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Category;
import org.springframework.data.domain.Page;

public interface CategoryService {
    void create(String name) throws BiNewsianException;
    void update(Long id, String name) throws BiNewsianException;
    void delete(Long id) throws BiNewsianException;
    Page<Category> findPaginated(int page, int size);
}
