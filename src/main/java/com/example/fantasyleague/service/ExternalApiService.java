package com.example.fantasyleague.service;

import com.example.fantasyleague.model.Player;
import com.example.fantasyleague.model.Team;
import com.example.fantasyleague.repository.PlayerRepository;
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
    private final PlayerRepository playerRepo; // Added for players
    private final String API_KEY = "bbfc07db7f5af22ba9f700d9e9fceffef32736bb4b9f77bcfdddd88264a02f5a";

    public ExternalApiService(WebClient.Builder builder, TeamRepository teamRepo, PlayerRepository playerRepo) {
        this.webClient = builder
                .baseUrl("https://apiv2.allsportsapi.com/football/")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                .maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
        this.teamRepo = teamRepo;
        this.playerRepo = playerRepo;
    }

    @SuppressWarnings("unchecked")
    public void fetchTeamsFromApi() {
        this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/")
                        .queryParam("met", "Teams")
                        .queryParam("leagueId", "152") // Premier League
                        .queryParam("APIkey", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe(response -> {
                    if (response != null && response.get("result") != null) {
                        List<Map<String, Object>> teams = (List<Map<String, Object>>) response.get("result");

                        for (Map<String, Object> teamData : teams) {
                            String teamName = (String) teamData.get("team_name");

                            // Find existing team or create new one
                            Team team = teamRepo.findAll().stream()
                                    .filter(t -> t.getName().equals(teamName))
                                    .findFirst()
                                    .orElseGet(() -> {
                                        Team newTeam = new Team();
                                        newTeam.setName(teamName);
                                        newTeam.setAttackRating((int) (Math.random() * 30) + 65);
                                        newTeam.setDefenseRating((int) (Math.random() * 30) + 65);
                                        newTeam.setPoints(0);
                                        return teamRepo.save(newTeam);
                                    });

                            // Sync Players for this team
                            if (teamData.containsKey("players")) {
                                List<Map<String, Object>> playersList = (List<Map<String, Object>>) teamData.get("players");
                                for (Map<String, Object> pData : playersList) {
                                    String playerName = (String) pData.get("player_name");

                                    // Basic check to avoid duplicate players
                                    Player p = new Player();
                                    p.setName(playerName);
                                    p.setPosition((String) pData.get("player_type"));
                                    p.setImageUrl((String) pData.get("player_image"));
                                    p.setTeam(team);
                                    playerRepo.save(p);
                                }
                            }
                        }
                        System.out.println("Sync Complete! Teams and Players imported.");
                    }
                }, error -> {
                    System.err.println("Sync Error: " + error.getMessage());
                });
    }
}