package com.example.fantasyleague.service;

import com.example.fantasyleague.model.*;
import com.example.fantasyleague.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException; // FIX: Added import
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

    // Your active API Key
    private final String API_KEY = "2e4d82534145853baa0ced2da087c764923fe13f650f75075197b7cc16486403";

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

    private Team findTeamLoosely(String apiName) {
        if (apiName == null || apiName.trim().isEmpty()) return null;

        // 1. Normalize the incoming API name
        String searchName = apiName.toLowerCase()
                .replace("manchester utd", "manchester united")
                .replace("man utd", "manchester united")
                .replace("wolves", "wolverhampton");

        return teamRepo.findAll().stream()
                .filter(t -> {
                    if (t.getName() == null) return false;

                    // 2. Normalize the Database name
                    String dbName = t.getName().toLowerCase()
                            .replace("manchester utd", "manchester united")
                            .replace("man utd", "manchester united")
                            .replace("wolves", "wolverhampton");

                    // 3. Compare the normalized strings
                    return dbName.contains(searchName) || searchName.contains(dbName);
                })
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public void fetchTeamsFromApi() {
        try {
            Map response = this.webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/")
                            .queryParam("met", "Teams")
                            .queryParam("leagueId", teamString)
                            .queryParam("APIkey", API_KEY)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .retry(3)
                    .block(); // Synchronous execution

            if (response != null && response.get("result") != null) {
                Object resultObj = response.get("result");
                if (resultObj instanceof List) {
                    List<Map<String, Object>> teams = (List<Map<String, Object>>) resultObj;
                    for (Map<String, Object> teamData : teams) {
                        String teamName = (String) teamData.get("team_name");
                        if (teamName == null) continue;

                        Team team = findTeamLoosely(teamName);
                        if (team == null) {
                            team = new Team();
                            team.setName(teamName);
                            try {
                                team = teamRepo.save(team); // FIX: Save immediately to establish identity
                            } catch (DataIntegrityViolationException e) {
                                // Race condition caught: another thread just created this team
                                team = teamRepo.findByName(teamName);
                            }
                        }

                        team.setLogoUrl((String) teamData.get("team_logo"));
                        teamRepo.save(team);

                        if (teamData.containsKey("players") && teamData.get("players") instanceof List) {
                            List<Map<String, Object>> playersList = (List<Map<String, Object>>) teamData.get("players");
                            for (Map<String, Object> pData : playersList) {
                                String playerName = (String) pData.get("player_name");
                                if (playerName == null) continue;

                                Player p = playerRepo.findAll().stream()
                                        .filter(existing -> existing.getName() != null && existing.getName().equals(playerName))
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
            }
        } catch (Exception e) {
            System.err.println("Error fetching teams: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void fetchOfficialStandings() {
        try {
            Map response = this.webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/")
                            .queryParam("met", "Standings")
                            .queryParam("leagueId", teamString)
                            .queryParam("APIkey", API_KEY)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .retry(3)
                    .block(); // Synchronous execution

            if (response != null && response.get("result") != null) {
                Object resultObj = response.get("result");
                List<Map<String, Object>> standings = null;

                if (resultObj instanceof Map) {
                    standings = (List<Map<String, Object>>) ((Map<String, Object>) resultObj).get("total");
                } else if (resultObj instanceof List) {
                    List<?> listObj = (List<?>) resultObj;
                    if (!listObj.isEmpty() && listObj.get(0) instanceof Map && ((Map<?, ?>) listObj.get(0)).containsKey("total")) {
                        standings = (List<Map<String, Object>>) ((Map<String, Object>) listObj.get(0)).get("total");
                    } else {
                        standings = (List<Map<String, Object>>) listObj;
                    }
                }

                if (standings != null) {
                    for (Map<String, Object> sData : standings) {
                        if (String.valueOf(sData.get("fk_stage_key")).equals("6") || sData.get("fk_stage_key") == null) {
                            String apiTeamName = (String) sData.get("standing_team");
                            if (apiTeamName == null) continue;

                            Team team = findTeamLoosely(apiTeamName);
                            if (team == null) {
                                team = new Team();
                                team.setName(apiTeamName);
                                team.setAttackRating(70);
                                team.setDefenseRating(70);

                                try {
                                    team = teamRepo.save(team); // FIX: Try to establish identity immediately
                                } catch (DataIntegrityViolationException e) {
                                    // FIX: Race condition caught: another thread just created this team
                                    team = teamRepo.findByName(apiTeamName);
                                }
                            }

                            team.setPoints(Integer.parseInt(String.valueOf(sData.getOrDefault("standing_PTS", 0))));
                            team.setMatchesPlayed(Integer.parseInt(String.valueOf(sData.getOrDefault("standing_P", 0))));
                            team.setWins(Integer.parseInt(String.valueOf(sData.getOrDefault("standing_W", 0))));
                            team.setDraws(Integer.parseInt(String.valueOf(sData.getOrDefault("standing_D", 0))));
                            team.setLosses(Integer.parseInt(String.valueOf(sData.getOrDefault("standing_L", 0))));
                            team.setGoalsFor(Integer.parseInt(String.valueOf(sData.getOrDefault("standing_F", 0))));
                            team.setGoalsAgainst(Integer.parseInt(String.valueOf(sData.getOrDefault("standing_A", 0))));
                            team.setGoalDifference(Integer.parseInt(String.valueOf(sData.getOrDefault("standing_GD", 0))));

                            teamRepo.save(team);
                        }
                    }
                    System.out.println("Standings Synced.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error syncing standings: " + e.getMessage());
        }
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
                    .retry(3)
                    .block();

            if (response != null && response.get("result") != null) {
                Object resultObj = response.get("result");

                if (resultObj instanceof List) {
                    List<Map<String, Object>> fixtures = (List<Map<String, Object>>) resultObj;

                    for (Map<String, Object> fData : fixtures) {
                        Object eventKeyObj = fData.get("event_key");

                        if (eventKeyObj == null) {
                            continue;
                        }

                        Long eventKey = Long.parseLong(String.valueOf(eventKeyObj));

                        // FIX: Only save if it doesn't exist
                        if (!fixtureRepo.existsById(eventKey)) {
                            Fixture f = new Fixture();
                            f.setId(eventKey);

                            f.setHomeTeam(findTeamLoosely((String) fData.get("event_home_team")));
                            f.setAwayTeam(findTeamLoosely((String) fData.get("event_away_team")));
                            f.setMatchDate(LocalDate.parse((String) fData.get("event_date")));

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
                    }
                    System.out.println("Fixtures synced for: " + fromDate + " to " + toDate);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching fixtures: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void fetchInjuries() {
        try {
            Map response = this.webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/")
                            .queryParam("met", "Injuries")
                            .queryParam("leagueId", teamString)
                            .queryParam("APIkey", API_KEY)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .retry(3)
                    .block(); // Synchronous execution

            if (response != null && response.get("result") != null) {
                Object resultObj = response.get("result");
                if (resultObj instanceof List) {
                    List<Map<String, Object>> result = (List<Map<String, Object>>) resultObj;
                    playerRepo.findAll().forEach(p -> {
                        p.setInjured(false);
                        p.setInjuryType(null);
                        playerRepo.save(p);
                    });
                    for (Map<String, Object> iData : result) {
                        String apiPlayerName = (String) iData.get("player_name");
                        if (apiPlayerName == null) continue;

                        playerRepo.findAll().stream()
                                .filter(p -> p.getName() != null && (
                                        p.getName().toLowerCase().contains(apiPlayerName.toLowerCase()) ||
                                                apiPlayerName.toLowerCase().contains(p.getName().toLowerCase())
                                ))
                                .findFirst()
                                .ifPresent(p -> {
                                    p.setInjured(true);
                                    p.setInjuryType((String) iData.get("injury_reason"));
                                    playerRepo.save(p);
                                });
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching injuries: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void fetchTopScorers() {
        try {
            Map response = this.webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/")
                            .queryParam("met", "Topscorers")
                            .queryParam("leagueId", teamString)
                            .queryParam("APIkey", API_KEY)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .retry(3)
                    .block(); // Synchronous execution

            if (response != null && response.get("result") != null) {
                Object resultObj = response.get("result");
                if (resultObj instanceof List) {
                    List<Map<String, Object>> scorers = (List<Map<String, Object>>) resultObj;
                    for (Map<String, Object> sData : scorers) {
                        String playerName = (String) sData.get("player_name");
                        if (playerName == null) continue;

                        playerRepo.findAll().stream()
                                .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(playerName))
                                .findFirst()
                                .ifPresent(p -> {
                                    Object goalsObj = sData.get("goals");
                                    p.setGoals(goalsObj != null ? Integer.parseInt(String.valueOf(goalsObj)) : 0);
                                    Object assistsObj = sData.get("assists");
                                    p.setAssists(assistsObj != null ? Integer.parseInt(String.valueOf(assistsObj)) : 0);
                                    playerRepo.save(p);
                                });
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching top scorers: " + e.getMessage());
        }
    }

    public void fetchStandings() {
        fetchOfficialStandings();
    }
}