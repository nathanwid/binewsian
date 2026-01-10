package com.binewsian.service;

import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.User;

public interface UserService {
    void changePassword(Long userId, String oldPassword, String newPassword) throws BiNewsianException;
    User updateProfile(Long userId, String username, String email) throws BiNewsianException;
}
