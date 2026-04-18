package com.binewsian.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.binewsian.client.ResendClient;
import okhttp3.OkHttpClient;

import java.time.Duration;

@Configuration
public class ResendConfig {

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Bean
    public ResendClient resendClient(OkHttpClient client) {
        return new ResendClient(client, apiKey, fromEmail);
    }
}
