package com.binewsian.client;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class ResendClient {

    private static final String BASE_URL = "https://api.resend.com/emails";
    private static final MediaType JSON = MediaType.parse("application/json");

    private final OkHttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;
    private final String fromEmail;

    public ResendClient(OkHttpClient client, String apiKey, String fromEmail) {
        this.client = client;
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
    }

    public void sendEmail(String to, String subject, String html) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Email 'to' cannot be empty");
        }

        log.info("Sending email to {}", to);

        try {
            Map<String, Object> body = Map.of(
                    "from", fromEmail,
                    "to", List.of(to),
                    "subject", subject,
                    "html", html
            );

            String json = mapper.writeValueAsString(body);

            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(json, JSON))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    throw new RuntimeException("Resend failed: " + responseBody);
                }

                log.info("Email successfully sent to {}", to);
            }

        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}