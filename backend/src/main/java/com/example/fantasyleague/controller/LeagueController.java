package com.example.fantasyleague.controller;

import com.example.fantasyleague.model.Fixture;
import com.example.fantasyleague.model.Player;
import com.example.fantasyleague.model.Team;
import com.example.fantasyleague.repository.FixtureRepository;
import com.example.fantasyleague.repository.TeamRepository;
import com.example.fantasyleague.service.ExternalApiService;
import com.example.fantasyleague.service.FixtureGenerator;
import com.example.fantasyleague.service.LeagueService;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/league")
@CrossOrigin(origins = "*")
public class LeagueController {

    private final LeagueService leagueService;
    private final TeamRepository teamRepo;
    private final FixtureRepository fixtureRepo;
    private final FixtureGenerator fixtureGenerator;
    private final ExternalApiService externalApiService;

    public LeagueController(LeagueService leagueService,
                            TeamRepository teamRepo,
                            FixtureRepository fixtureRepo,
                            FixtureGenerator fixtureGenerator,
                            ExternalApiService externalApiService) {
        this.leagueService = leagueService;
        this.teamRepo = teamRepo;
        this.fixtureRepo = fixtureRepo;
        this.fixtureGenerator = fixtureGenerator;
        this.externalApiService = externalApiService;
    }

    @GetMapping("/health")
    public String health() { return "UP"; }

    @PostMapping("/simulate")
    public void simulateToday() {
        leagueService.simulateTodayMatches();
    }

    @PostMapping("/generate-fixtures")
    public String generateFixtures() {
        fixtureGenerator.generateSeason();
        return "Season fixtures generated!";
    }

    @GetMapping("/standings")
    public List<Team> getStandings() {
        externalApiService.fetchOfficialStandings();
        return teamRepo.findAll();
    }

    // FIXED: Smart Fixture Fetching
    @GetMapping("/fixtures")
    public List<Fixture> getFixtures(
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to
    ) {
        if (from == null || to == null) {
            from = LocalDate.now().minusDays(1).toString();
            to = LocalDate.now().plusDays(6).toString();
        }

        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);

        // 1. Check local DB
        List<Fixture> dbFixtures = fixtureRepo.findByDateRange(start, end);

        // 2. If empty, fetch from API and save
        if (dbFixtures.isEmpty()) {
            System.out.println("Cache miss for " + from + ". Fetching from API...");
            externalApiService.fetchRealFixtures(from, to);
            return fixtureRepo.findByDateRange(start, end);
        }

        return dbFixtures;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void refreshData() {
        externalApiService.fetchOfficialStandings();
        String today = LocalDate.now().toString();
        externalApiService.fetchRealFixtures(today, today);
    }

    @GetMapping("/clubs")
    public List<Team> getClubs() {
        return teamRepo.findAll();
    }

    @GetMapping("/clubs/{id}")
    public Team getClubDetails(@PathVariable Long id) {
        return teamRepo.findById(id).orElseThrow();
    }

    @GetMapping("/clubs/{id}/squad")
    public List<Player> getClubSquad(@PathVariable Long id) {
        Team team = teamRepo.findById(id).orElseThrow();
        return team.getSquad();
    }

    @GetMapping("/clubs/{id}/fixtures")
    public List<Fixture> getClubFixtures(@PathVariable Long id) {
        return fixtureRepo.findAll().stream()
                .filter(f -> f.getHomeTeam().getId().equals(id) || f.getAwayTeam().getId().equals(id))
                .toList();
    }

    @PostMapping("/sync-teams")
    public String syncTeams() {
        externalApiService.fetchTeamsFromApi();
        return "Sync started...";
    }

    @PostMapping("/sync-standings")
    public ResponseEntity<Void> syncStandings() {
        externalApiService.fetchOfficialStandings();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/fixtures/sync")
    public ResponseEntity<Map<String, String>> syncFixturesByDate(
            @RequestParam("from") String from,
            @RequestParam("to") String to) {
        externalApiService.fetchRealFixtures(from, to);
        return ResponseEntity.ok(Map.of("message", "Syncing matches for: " + from + " to " + to));
    }

    @PostMapping("/sync-injuries")
    public ResponseEntity<Map<String, String>> syncInjuries() {
        externalApiService.fetchInjuries();
        return ResponseEntity.ok(Map.of("message", "Injuries sync started"));
    }

    @PostMapping("/sync-topscorers")
    public ResponseEntity<Map<String, String>> syncTopScorers() {
        externalApiService.fetchTopScorers();
        return ResponseEntity.ok(Map.of("message", "Top Scorers sync started"));
    }

    @PostMapping("/switch-league/{id}")
    public ResponseEntity<Map<String, String>> switchLeague(@PathVariable String id) {
        externalApiService.setTeamString(id);
        return ResponseEntity.ok(Map.of("message", "League context switched to: " + id));
    }
}