package com.example.fantasyleague.service;

import com.example.fantasyleague.model.Team;
import com.example.fantasyleague.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Map;

@Service
public class ExternalApiService {

    private final WebClient webClient;
    private final TeamRepository teamRepo;
    private final String API_KEY = "bbfc07db7f5af22ba9f700d9e9fceffef32736bb4b9f77bcfdddd88264a02f5a";

    public ExternalApiService(WebClient.Builder webClientBuilder, TeamRepository teamRepo) {
        this.webClient = webClientBuilder.baseUrl("https://apiv2.allsportsapi.com/football/").build();
        this.teamRepo = teamRepo;
    }

    @SuppressWarnings("unchecked")
    public void fetchTeamsFromApi() {
        this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/")
                        .queryParam("action", "get_teams")      // Changed from 'metropol'
                        .queryParam("leagueId", "152")         // 152 is usually the Premier League ID
                        .queryParam("APIkey", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe(response -> {
                    // Check if the API returned a 'result' list
                    if (response != null && response.containsKey("result")) {
                        List<Map<String, Object>> result = (List<Map<String, Object>>) response.get("result");
                        for (Map<String, Object> teamData : result) {
                            Team team = new Team();
                            team.setName((String) teamData.get("team_name"));
                            // Use team_key if your model needs a unique external ID
                            team.setAttackRating((int) (Math.random() * 40) + 50);
                            team.setDefenseRating((int) (Math.random() * 40) + 50);
                            team.setPoints(0);
                            teamRepo.save(team);
                        }
                    } else {
                        System.out.println("API Error: " + response.get("message"));
                    }
                }, error -> {
                    // This prevents the 'reactor.core.Exceptions$ErrorCallbackNotImplemented' error
                    System.err.println("Failed to fetch teams: " + error.getMessage());
                });
    }
}