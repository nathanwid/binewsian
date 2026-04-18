package com.binewsian.email;

import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class EmailQueueService {

    private final BlockingQueue<EmailEvent> queue = new LinkedBlockingQueue<>();

    public void enqueue(EmailEvent event) {
        queue.offer(event);
    }

    public EmailEvent take() throws InterruptedException {
        return queue.take();
    }
}