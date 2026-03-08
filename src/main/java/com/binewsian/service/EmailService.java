package com.binewsian.service;

import com.binewsian.model.User;

import java.util.Map;

public interface EmailService {
    void sendCredentials(String email, String username, String rawPassword);
    void sendResetPassword(String email, String token, String appUrl);
    void sendContentNotification(User user, Map<String, Object> data);
}
