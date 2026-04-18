package com.binewsian.service;

import com.binewsian.model.User;

import java.util.List;
import java.util.Map;

public interface EmailService {
    void sendCredentials(String email, String username, String rawPassword);
    void sendResetPassword(String email, String token, String appUrl);
    void sendContentNotification(List<User> users, Map<String, Object> data);
}
