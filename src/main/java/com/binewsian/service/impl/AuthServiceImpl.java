package com.binewsian.service.impl;

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
        if (userRepository.existsByUsername(username)) {
            throw new BiNewsianException("Username sudah digunakan");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BiNewsianException("Email sudah digunakan");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(Role.USER);

        userRepository.save(user);
    }

    @Override
    public User authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        User user = null;

        if (userOpt.isPresent()) {
            user = userOpt.get();
        }

        if (user != null) {
            if (!user.isEnabled()) {
                return null;
            }
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }

        return null;
    }

}
