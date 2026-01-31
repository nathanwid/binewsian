package com.binewsian.service;

import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.User;

import java.util.Map;

public interface EmailService {
    void sendCredentials(String email, String username, String rawPassword) throws BiNewsianException;
    void sendResetPassword(String email, String token, String appUrl) throws BiNewsianException;
    void sendContentNotification(User user, Map<String, Object> data) throws BiNewsianException;
}
