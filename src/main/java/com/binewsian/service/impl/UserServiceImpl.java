package com.binewsian.service.impl;

import com.binewsian.constant.AppConstant;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.User;
import com.binewsian.repository.UserRepository;
import com.binewsian.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) throws BiNewsianException {
        User user = userRepository.findById(userId).orElseThrow(() -> new BiNewsianException(AppConstant.USER_NOT_FOUND));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BiNewsianException("Old password is incorrect");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BiNewsianException("New password must be different from old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public User updateProfile(Long userId, String username, String email) throws BiNewsianException {
        User user = userRepository.findById(userId).orElseThrow(() -> new BiNewsianException(AppConstant.USER_NOT_FOUND));

        if (!user.getUsername().equalsIgnoreCase(username)) {
            Optional<User> existingUser = userRepository.findByUsernameAllIgnoreCase(username);
            if (existingUser.isPresent()) {
                throw new BiNewsianException("Username already taken");
            }
        }

        if (!user.getEmail().equalsIgnoreCase(email)) {
            Optional<User> existingUser = userRepository.findByEmail(email.toLowerCase());
            if (existingUser.isPresent()) {
                throw new BiNewsianException("Email already taken");
            }
        }

        user.setUsername(username);
        user.setEmail(email.toLowerCase());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

}
