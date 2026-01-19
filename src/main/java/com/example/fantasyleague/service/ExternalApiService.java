package com.example.fantasyleague.service;

import com.example.fantasyleague.model.Team;
import com.example.fantasyleague.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Map;

@Service
public class ExternalApiService {

    private final WebClient webClient;
    private final TeamRepository teamRepo;
    private final String API_KEY = "bbfc07db7f5af22ba9f700d9e9fceffef32736bb4b9f77bcfdddd88264a02f5a";

    public ExternalApiService(WebClient.Builder builder, TeamRepository teamRepo) {
        // Increase the buffer limit to 16MB
        this.webClient = builder
                .baseUrl("https://apiv2.allsportsapi.com/football/")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                .maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
        this.teamRepo = teamRepo;
    }



    @SuppressWarnings("unchecked")
    public void fetchTeamsFromApi() {
        this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/")
                        .queryParam("met", "Teams")
                        .queryParam("leagueId", "152")
                        .queryParam("APIkey", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe(response -> {
                    if (response != null && response.get("result") != null) {
                        List<Map<String, Object>> teams = (List<Map<String, Object>>) response.get("result");

                        for (Map<String, Object> teamData : teams) {
                            // Check if team already exists by name to avoid duplicates
                            String teamName = (String) teamData.get("team_name");
                            if (teamRepo.findAll().stream().noneMatch(t -> t.getName().equals(teamName))) {
                                Team team = new Team();
                                team.setName(teamName);
                                team.setAttackRating((int) (Math.random() * 30) + 65);
                                team.setDefenseRating((int) (Math.random() * 30) + 65);
                                team.setPoints(0);
                                teamRepo.save(team);
                            }
                        }
                        System.out.println("Sync Complete! Imported teams: " + teams.size());
                    }
                }, error -> {
                    System.err.println("Sync Error: " + error.getMessage());
                });
    }

    public void testApiWithDifferentCalls() {
        // Note the change from 'action' to 'met' as per doc version 2.1
        String url = "https://apiv2.allsportsapi.com/football/?met=Countries&APIkey=" + API_KEY;

        System.out.println("Testing URL: " + url);

        this.webClient.get()
                .uri(url)
                .header("User-Agent", "Mozilla/5.0")
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(response -> {
                    System.out.println("--- SUCCESS ---");
                    System.out.println(response);
                }, error -> {
                    System.err.println("--- FAILED ---");
                    System.err.println("Error: " + error.getMessage());
                });
    }
}