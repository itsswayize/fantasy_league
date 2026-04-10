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

    // ==========================================
    // ⚡ INSTANT READS (FRONTEND -> DATABASE CACHE)
    // ==========================================

    @GetMapping("/standings")
    public List<Team> getStandings() {
        List<Team> teams = teamRepo.findAll();

        // Failsafe: If the database is empty (e.g. fresh start), fetch it once.
        if (teams.isEmpty()) {
            System.out.println("Database empty. Initializing standings from API...");
            externalApiService.fetchOfficialStandings();
            return teamRepo.findAll();
        }

        // Normal behavior: INSTANT response from PostgreSQL. No API wait time.
        return teams;
    }

    @GetMapping("/fixtures")
    public List<Fixture> getFixtures(
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to
    ) {
        if (from == null || to == null) {
            from = LocalDate.now().minusDays(1).toString();
            to = LocalDate.now().plusDays(14).toString(); // Default 2 weeks view
        }

        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);

        List<Fixture> dbFixtures = fixtureRepo.findByDateRange(start, end);

        boolean isDummyData = !dbFixtures.isEmpty() && dbFixtures.stream()
                .anyMatch(f -> f.getMatchTime() == null || f.getMatchTime().equals("00:00"));

        // Failsafe: Only fetch if database is empty or has generated dummy data
        if (dbFixtures.isEmpty() || isDummyData) {
            System.out.println("No valid fixtures in DB for dates. Fetching from API...");
            if (!dbFixtures.isEmpty()) {
                fixtureRepo.deleteByMatchDateBetween(start, end);
            }
            externalApiService.fetchRealFixtures(from, to);
            return fixtureRepo.findByDateRange(start, end);
        }

        // Normal behavior: INSTANT response from PostgreSQL.
        return dbFixtures;
    }

    @GetMapping("/clubs")
    public List<Team> getClubs() {
        return teamRepo.findAll();
    }


    // ==========================================
    // ⚙️ THE BACKGROUND WORKER (SCHEDULED + DB)
    // ==========================================

    // Runs automatically at the start of every hour (00:00, 01:00, 02:00, etc.)
    @Scheduled(cron = "0 0 * * * *")
    public void hourlyBackgroundSync() {
        System.out.println("Running hourly background sync to update Database...");

        // 1. Update the standings quietly
        externalApiService.fetchOfficialStandings();

        // 2. Fetch matches for yesterday, today, and the next 14 days
        String from = LocalDate.now().minusDays(1).toString();
        String to = LocalDate.now().plusDays(14).toString();

        // This quietly updates the database. The frontend doesn't have to wait for this!
        externalApiService.fetchRealFixtures(from, to);
    }


    // ==========================================
    // OTHER STANDARD ENDPOINTS
    // ==========================================

    @PostMapping("/simulate")
    public void simulateToday() {
        leagueService.simulateTodayMatches();
    }

    @PostMapping("/generate-fixtures")
    public String generateFixtures() {
        fixtureGenerator.generateSeason();
        return "Season fixtures generated!";
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