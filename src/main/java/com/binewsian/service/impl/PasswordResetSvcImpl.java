package com.binewsian.service.impl;

import com.binewsian.constant.AppConstant;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.PasswordResetToken;
import com.binewsian.model.User;
import com.binewsian.repository.PasswordResetTokenRepository;
import com.binewsian.repository.UserRepository;
import com.binewsian.service.EmailService;
import com.binewsian.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetSvcImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void createPasswordResetTokenForUser(String email, String appUrl) throws BiNewsianException {
        User user = userRepository.findByEmail(email.toLowerCase()).orElseThrow(() -> new BiNewsianException("User not found with email: " + email));

        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

        // Create new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        tokenRepository.save(resetToken);

        // Send reset password email
        emailService.sendResetPassword(user.getEmail(), token, appUrl);
    }

    @Override
    public boolean validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);
        return resetToken.filter(passwordResetToken -> !passwordResetToken.isExpired()).isPresent();
    }

    @Override
    @Transactional
    public void updatePassword(String token, String newPassword) throws BiNewsianException {
        Optional<PasswordResetToken> resetTokenOpt = tokenRepository.findByToken(token);

        if (!resetTokenOpt.isPresent() || resetTokenOpt.get().isExpired()) {
            throw new BiNewsianException(AppConstant.INVALID_ERROR_PASSWORD_RESET_LINK);
        }

        PasswordResetToken resetToken = resetTokenOpt.get();
        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete used token
        tokenRepository.delete(resetToken);
    }

}
