package com.binewsian.service.impl;

import com.binewsian.constant.AppConstant;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.Category;
import com.binewsian.repository.CategoryRepository;
import com.binewsian.service.CategoryService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public void create(String name) throws BiNewsianException {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BiNewsianException(AppConstant.CATEGORY_ALREADY_EXISTS);
        }

        Category category = new Category();
        category.setName(formatName(name));
        categoryRepository.save(category);
    }

    @Override
    public void update(Long id, String name) throws BiNewsianException {
        String formattedName = formatName(name);

        Category category = categoryRepository.findById(id).orElseThrow(() -> new BiNewsianException(AppConstant.CATEGORY_NOT_FOUND));

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(formattedName, id)) {
            throw new BiNewsianException(AppConstant.CATEGORY_ALREADY_EXISTS);
        }

        category.setName(formattedName);
        categoryRepository.save(category);
    }

    @Override
    public void delete(Long id) throws BiNewsianException {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new BiNewsianException(AppConstant.CATEGORY_NOT_FOUND));

        try {
            categoryRepository.delete(category);
        } catch (DataIntegrityViolationException ex) {
            throw new BiNewsianException("Category is still in use and cannot be deleted.");
        }
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Page<Category> findPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return categoryRepository.findAll(pageable);
    }

    private String formatName(String name) {
        name = name.trim().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

}
