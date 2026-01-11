package com.binewsian.service.impl;

import com.binewsian.constant.AppConstant;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.enums.Role;
import com.binewsian.model.User;
import com.binewsian.repository.UserRepository;
import com.binewsian.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void register(String username, String password, String email) throws BiNewsianException {
        if (userRepository.existsByUsernameAllIgnoreCase(username)) {
            throw new BiNewsianException(AppConstant.USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmailAllIgnoreCase(email)) {
            throw new BiNewsianException(AppConstant.EMAIL_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email.toLowerCase());
        user.setRole(Role.USER);

        userRepository.save(user);
    }

    @Override
    public User authenticate(String email, String password) throws BiNewsianException {
        User user = userRepository.findByEmail(email.toLowerCase()).orElseThrow(() -> new BiNewsianException(AppConstant.USER_NOT_FOUND));

        if (!user.isEnabled()) {
            throw new BiNewsianException(AppConstant.USER_HAS_BEEN_DEACTIVATED);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BiNewsianException(AppConstant.INCORRECT_EMAIL_PASSWORD);
        }

        return user;
    }

}
