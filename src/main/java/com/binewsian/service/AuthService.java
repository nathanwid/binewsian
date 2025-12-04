package com.binewsian.service;

import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.User;

public interface AuthService {
    void register(String username, String password, String email) throws BiNewsianException;
    User authenticate(String username, String password);
}
