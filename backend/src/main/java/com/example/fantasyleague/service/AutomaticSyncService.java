package com.example.fantasyleague.service;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AutomaticSyncService {

    private final ExternalApiService externalApiService;

    public AutomaticSyncService(ExternalApiService externalApiService) {
        this.externalApiService = externalApiService;
    }

    // 1. This runs automatically exactly once when the Spring Boot server starts up
    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        System.out.println("=== INITIALIZING STARTUP DATA SYNC ===");

        // CRITICAL ORDER: Fetch teams first so the database gets the logos
        externalApiService.fetchTeamsFromApi();

        // Fetch standings second so it just updates the points of the teams with logos
        externalApiService.fetchOfficialStandings();

        // Optionally fetch the rest of your data on startup
        // externalApiService.fetchInjuries();
        // externalApiService.fetchTopScorers();

        System.out.println("=== STARTUP DATA SYNC COMPLETE ===");
    }

    // 2. This runs automatically in the background every 12 hours to keep data fresh
    @Scheduled(fixedRate = 43200000)
    public void syncPeriodically() {
        System.out.println("=== RUNNING SCHEDULED BACKGROUND SYNC ===");
        externalApiService.fetchTeamsFromApi();
        externalApiService.fetchOfficialStandings();
    }
}