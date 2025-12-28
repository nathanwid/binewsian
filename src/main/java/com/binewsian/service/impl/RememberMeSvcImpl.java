package com.binewsian.service.impl;

import com.binewsian.model.RememberMeToken;
import com.binewsian.model.User;
import com.binewsian.repository.RememberMeTokenRepository;
import com.binewsian.repository.UserRepository;
import com.binewsian.service.RememberMeSvc;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RememberMeSvcImpl implements RememberMeSvc {

    private final RememberMeTokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Override
    public String createToken(String email) {
        // Generate random token
        String token = UUID.randomUUID().toString();

        // Delete old token for this user
        deleteTokenByEmail(email);

        // Create new token (expires in 7 days)
        RememberMeToken rememberMeToken = new RememberMeToken(
                token,
                email,
                LocalDateTime.now().plusDays(7)
        );

        tokenRepository.save(rememberMeToken);

        return token;
    }

    @Override
    public User validateTokenAndGetUser(String token) {
        Optional<RememberMeToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            return null;
        }

        RememberMeToken rememberMeToken = tokenOpt.get();

        // Check if token expired
        if (rememberMeToken.isExpired()) {
            tokenRepository.delete(rememberMeToken);
            return null;
        }

        // Get user
        Optional<User> userOpt = userRepository.findByEmail(rememberMeToken.getEmail());

        return userOpt.orElse(null);
    }

    @Override
    public void deleteTokenByEmail(String email) {
        tokenRepository.deleteByEmail(email);
    }

}
