package com.binewsian.service;

import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ContributorService {
    void create(String username, String email) throws BiNewsianException;
    Page<User> findContributorPaginated(int page, int size);
    List<User> findAll();
}
