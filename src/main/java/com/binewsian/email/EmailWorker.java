package com.binewsian.email;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.binewsian.client.ResendClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailWorker {

    private final EmailQueueService queueService;
    private final ResendClient resendClient;

    @PostConstruct
    public void startWorker() {
        Thread worker = new Thread(() -> {
            log.info("Email Worker started...");

            while (true) {
                try {
                    EmailEvent event = queueService.take();

                    resendClient.sendEmail(
                            event.to(),
                            event.subject(),
                            event.html()
                    );

                    Thread.sleep(600);
                } catch (Exception e) {
                    log.error("Email worker error", e);
                }
            }
        });

        worker.setDaemon(true);
        worker.start();
    }
}