package com.example.fantasyleague.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class KeepAliveService {
    private static final Logger logger = LoggerFactory.getLogger(KeepAliveService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    // Runs every 14 minutes (840,000 milliseconds)
    @Scheduled(fixedRate = 840000)
    public void keepAlive() {
        try {
            // Use your actual Render URL
            String url = "https://fpl-backend-em6n.onrender.com/api/league/health";
            String response = restTemplate.getForObject(url, String.class);
            logger.info("Self-ping successful. Status: " + response);
        } catch (Exception e) {
            logger.error("Self-ping failed: " + e.getMessage());
        }
    }
}