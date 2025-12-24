package com.binewsian.service.impl;

import com.binewsian.enums.Role;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.User;
import com.binewsian.repository.UserRepository;
import com.binewsian.service.ContributorService;
import com.binewsian.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class ContributorServiceImpl implements ContributorService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public void create(String username, String email) throws BiNewsianException {
        if (userRepository.existsByUsername(username)) {
            throw new BiNewsianException("Username sudah digunakan");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BiNewsianException("Email sudah digunakan");
        }

        String rawPassword = generateRandomPassword();

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmail(email);
        user.setRole(Role.CONTRIBUTOR);

        // Send credentials to user
        try {
            emailService.sendCredentials(email, username, rawPassword);
        } catch (BiNewsianException e) {
            throw new BiNewsianException(e.getMessage());
        }

        userRepository.save(user);
    }

    @Override
    public Page<User> findContributorPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findByRole(Role.CONTRIBUTOR, pageable);
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int length = 8;

        StringBuilder password = new StringBuilder(length);
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

}
