package com.binewsian.service.impl;

import com.binewsian.model.User;
import com.binewsian.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    @Override
    public void sendCredentials(String email, String username, String rawPassword) {
        log.info("Sending credentials email to {}", email);

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8"
            );

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("password", rawPassword);

            String htmlContent = templateEngine.process(
                    "email/account-created",
                    context
            );

            helper.setTo(email);
            helper.setSubject("Your Contributor Account Credentials");
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Email successfully sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send email to {}", email, e);
        }
    }

    @Async
    @Override
    public void sendResetPassword(String email, String token, String appUrl) {
        log.info("Sending reset password email to {}", email);

        try {
            String resetUrl = appUrl + "/reset-password?token=" + token;

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8"
            );

            Context context = new Context();
            context.setVariable("email", email);
            context.setVariable("resetUrl", resetUrl);

            String htmlContent = templateEngine.process(
                    "email/password-reset",
                    context
            );

            helper.setTo(email);
            helper.setSubject("Password Reset Request");
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Email successfully sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send email to {}", email, e);
        }
    }

    @Async
    @Override
    public void sendContentNotification(User user, Map<String, Object> data) {
        log.info("Sending content notification email to {}", user.getEmail());

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8"
            );

            Context context = new Context();
            context.setVariable("email", user.getEmail());
            context.setVariable("contentType", data.get("contentType"));
            context.setVariable("username", user.getUsername());
            LocalDate activityDate = (LocalDate) data.get("activityDate");
            if (activityDate != null) {
                context.setVariable("activityDate", activityDate);
            }
            context.setVariable("author", data.get("author"));
            context.setVariable("contentTitle", data.get("contentTitle"));
            context.setVariable("contentDescription", data.get("contentDescription"));
            context.setVariable("contentUrl", data.get("contentUrl"));

            String htmlContent = templateEngine.process(
                    "email/content-notif",
                    context
            );

            helper.setTo(user.getEmail());
            helper.setSubject("New Content Published!");
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Email successfully sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email to {}", user.getEmail(), e);
        }
    }

}
