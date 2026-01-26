package com.example.fantasyleague.service;

import com.example.fantasyleague.model.*;
import com.example.fantasyleague.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ExternalApiService {

    private String teamString;
    private final WebClient webClient;
    private final TeamRepository teamRepo;
    private final PlayerRepository playerRepo;
    private final FixtureRepository fixtureRepo;

    private final String API_KEY = "bbfc07db7f5af22ba9f700d9e9fceffef32736bb4b9f77bcfdddd88264a02f5a";

    public ExternalApiService(@Value("${fpl.league.id:152}") String teamString,
                              WebClient.Builder builder,
                              TeamRepository teamRepo,
                              PlayerRepository playerRepo,
                              FixtureRepository fixtureRepo) {
        this.teamString = teamString;
        this.webClient = builder
                .baseUrl("https://apiv2.allsportsapi.com/football/")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
        this.teamRepo = teamRepo;
        this.playerRepo = playerRepo;
        this.fixtureRepo = fixtureRepo;
    }

    public void setTeamString(String teamString) {
        this.teamString = teamString;
    }

    // FIXED: Better team matching to prevent duplicates
    private Team findTeamLoosely(String apiName) {
        // 1. Try exact match first
        Team exact = teamRepo.findByName(apiName);
        if (exact != null) return exact;

        // 2. Fuzzy match
        return teamRepo.findAll().stream()
                .filter(t -> t.getName().equalsIgnoreCase(apiName) ||
                        t.getName().toLowerCase().contains(apiName.toLowerCase()) ||
                        apiName.toLowerCase().contains(t.getName().toLowerCase()) ||
                        (apiName.equals("Manchester Utd") && t.getName().contains("Manchester United")) ||
                        (apiName.equals("Wolves") && t.getName().contains("Wolverhampton")))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public void fetchTeamsFromApi() {
        this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/")
                        .queryParam("met", "Teams")
                        .queryParam("leagueId", teamString)
                        .queryParam("APIkey", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .retry(3)
                .subscribe(response -> {
                    if (response != null && response.get("result") != null) {
                        List<Map<String, Object>> teams = (List<Map<String, Object>>) response.get("result");
                        for (Map<String, Object> teamData : teams) {
                            String teamName = (String) teamData.get("team_name");
                            Team team = findTeamLoosely(teamName);
                            if (team == null) {
                                team = new Team();
                                team.setName(teamName);
                            }
                            team.setLogoUrl((String) teamData.get("team_logo"));
                            teamRepo.save(team);

                            if (teamData.containsKey("players")) {
                                List<Map<String, Object>> playersList = (List<Map<String, Object>>) teamData.get("players");
                                for (Map<String, Object> pData : playersList) {
                                    String playerName = (String) pData.get("player_name");
                                    Player p = playerRepo.findAll().stream()
                                            .filter(existing -> existing.getName().equals(playerName))
                                            .findFirst().orElse(new Player());

                                    p.setName(playerName);
                                    p.setPosition((String) pData.get("player_type"));
                                    p.setImageUrl((String) pData.get("player_image"));
                                    p.setTeam(team);

                                    String isInjured = (String) pData.get("player_injured");
                                    p.setInjured("Yes".equalsIgnoreCase(isInjured));

                                    if (p.isInjured()) {
                                        if (p.getInjuryType() == null || p.getInjuryType().equals("Unavailable")) {
                                            p.setInjuryType("Match Fitness / Assessment");
                                        }
                                    }
                                    playerRepo.save(p);
                                }
                            }
                        }
                        System.out.println("Teams and Players Synced.");
                    }
                }, error -> System.err.println("Error fetching teams: " + error.getMessage()));
    }

    @SuppressWarnings("unchecked")
    public void fetchOfficialStandings() {
        this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/")
                        .queryParam("met", "Standings")
                        .queryParam("leagueId", teamString)
                        .queryParam("APIkey", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe(response -> {
                    if (response != null && response.get("result") != null) {
                        Map<String, Object> result = (Map<String, Object>) response.get("result");
                        List<Map<String, Object>> standings = (List<Map<String, Object>>) result.get("total");
                        for (Map<String, Object> sData : standings) {
                            if (String.valueOf(sData.get("fk_stage_key")).equals("6")) {
                                String apiTeamName = (String) sData.get("standing_team");
                                Team team = findTeamLoosely(apiTeamName);
                                if (team == null) {
                                    team = new Team();
                                    team.setName(apiTeamName);
                                    team.setAttackRating(70);
                                    team.setDefenseRating(70);
                                }
                                team.setPoints(Integer.parseInt(String.valueOf(sData.get("standing_PTS"))));
                                team.setMatchesPlayed(Integer.parseInt(String.valueOf(sData.get("standing_P"))));
                                team.setWins(Integer.parseInt(String.valueOf(sData.get("standing_W"))));
                                team.setDraws(Integer.parseInt(String.valueOf(sData.get("standing_D"))));
                                team.setLosses(Integer.parseInt(String.valueOf(sData.get("standing_L"))));
                                team.setGoalsFor(Integer.parseInt(String.valueOf(sData.get("standing_F"))));
                                team.setGoalsAgainst(Integer.parseInt(String.valueOf(sData.get("standing_A"))));
                                team.setGoalDifference(Integer.parseInt(String.valueOf(sData.get("standing_GD"))));
                                teamRepo.save(team);
                            }
                        }
                        System.out.println("Standings Synced.");
                    }
                }, error -> System.err.println("Error syncing standings: " + error.getMessage()));
    }

    @SuppressWarnings("unchecked")
    public void fetchRealFixtures(String fromDate, String toDate) {
        try {
            Map response = this.webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/")
                            .queryParam("met", "Fixtures")
                            .queryParam("leagueId", teamString)
                            .queryParam("from", fromDate)
                            .queryParam("to", toDate)
                            .queryParam("APIkey", API_KEY)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.get("result") != null) {
                List<Map<String, Object>> fixtures = (List<Map<String, Object>>) response.get("result");
                for (Map<String, Object> fData : fixtures) {
                    Long eventKey = Long.parseLong(String.valueOf(fData.get("event_key")));

                    // Strictly use ID from API to prevent duplicates
                    Fixture f = fixtureRepo.findById(eventKey).orElse(new Fixture());
                    f.setId(eventKey);

                    f.setHomeTeam(findTeamLoosely((String) fData.get("event_home_team")));
                    f.setAwayTeam(findTeamLoosely((String) fData.get("event_away_team")));
                    f.setMatchDate(LocalDate.parse((String) fData.get("event_date")));

                    // Safe Time Parsing
                    Object timeObj = fData.get("event_time");
                    f.setMatchTime(timeObj != null ? (String) timeObj : "00:00");

                    f.setStatus((String) fData.get("event_status"));

                    String finalResult = String.valueOf(fData.get("event_final_result"));
                    if (finalResult != null && finalResult.contains(" - ")) {
                        String[] scores = finalResult.split(" - ");
                        f.setHomeScore(Integer.parseInt(scores[0].trim()));
                        f.setAwayScore(Integer.parseInt(scores[1].trim()));
                    }
                    f.setPlayed("Finished".equals(fData.get("event_status")));

                    if (f.getHomeTeam() != null && f.getAwayTeam() != null) {
                        fixtureRepo.save(f);
                    }
                }
                System.out.println("Fixtures synced for: " + fromDate + " to " + toDate);
            }
        } catch (Exception e) {
            System.err.println("Error fetching fixtures: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void fetchInjuries() {
        this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/")
                        .queryParam("met", "Injuries")
                        .queryParam("leagueId", teamString)
                        .queryParam("APIkey", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .retry(3)
                .subscribe(response -> {
                    if (response != null && response.get("result") != null) {
                        List<Map<String, Object>> result = (List<Map<String, Object>>) response.get("result");
                        playerRepo.findAll().forEach(p -> {
                            p.setInjured(false);
                            p.setInjuryType(null);
                            playerRepo.save(p);
                        });
                        for (Map<String, Object> iData : result) {
                            String apiPlayerName = (String) iData.get("player_name");
                            playerRepo.findAll().stream()
                                    .filter(p -> p.getName().toLowerCase().contains(apiPlayerName.toLowerCase()) ||
                                            apiPlayerName.toLowerCase().contains(p.getName().toLowerCase()))
                                    .findFirst()
                                    .ifPresent(p -> {
                                        p.setInjured(true);
                                        p.setInjuryType((String) iData.get("injury_reason"));
                                        playerRepo.save(p);
                                    });
                        }
                    }
                }, error -> System.err.println("Error fetching injuries: " + error.getMessage()));
    }

    @SuppressWarnings("unchecked")
    public void fetchTopScorers() {
        this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/")
                        .queryParam("met", "Topscorers")
                        .queryParam("leagueId", teamString)
                        .queryParam("APIkey", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .retry(3)
                .subscribe(response -> {
                    if (response != null && response.get("result") != null) {
                        List<Map<String, Object>> scorers = (List<Map<String, Object>>) response.get("result");
                        for (Map<String, Object> sData : scorers) {
                            String playerName = (String) sData.get("player_name");
                            playerRepo.findAll().stream()
                                    .filter(p -> p.getName().equalsIgnoreCase(playerName))
                                    .findFirst()
                                    .ifPresent(p -> {
                                        p.setGoals(Integer.parseInt(String.valueOf(sData.get("goals"))));
                                        Object assistsObj = sData.get("assists");
                                        p.setAssists(assistsObj != null ? Integer.parseInt(String.valueOf(assistsObj)) : 0);
                                        playerRepo.save(p);
                                    });
                        }
                    }
                }, error -> System.err.println("Error fetching top scorers: " + error.getMessage()));
    }

    public void fetchStandings() {
        fetchOfficialStandings();
    }
}