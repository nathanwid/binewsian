package com.binewsian.service.impl;

import com.binewsian.client.ResendClient;
import com.binewsian.email.EmailEvent;
import com.binewsian.email.EmailQueueService;
import com.binewsian.model.User;
import com.binewsian.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class EmailServiceImpl implements EmailService {

    private final EmailQueueService emailQueueService;
    private final SpringTemplateEngine templateEngine;

    @Override
    public void sendCredentials(String email, String username, String rawPassword) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("password", rawPassword);

        String html = templateEngine.process("email/account-created", context);

        EmailEvent event = new EmailEvent(email, "Your Contributor Account Credentials", html);
        emailQueueService.enqueue(event);
    }

    @Override
    public void sendResetPassword(String email, String token, String appUrl) {
        String resetUrl = appUrl + "/reset-password?token=" + token;

        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("resetUrl", resetUrl);

        String html = templateEngine.process("email/password-reset", context);

        EmailEvent event = new EmailEvent(email, "Password Reset Request", html);
        emailQueueService.enqueue(event);
    }

    @Override
    public void sendContentNotification(List<User> users, Map<String, Object> data) {
        for (User user : users) {
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

            String html = templateEngine.process("email/content-notif", context);

            EmailEvent event = new EmailEvent(user.getEmail(), "New Content Published!", html);
            emailQueueService.enqueue(event);
        }
    }
}
