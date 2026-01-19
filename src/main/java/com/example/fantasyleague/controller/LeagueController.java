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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/league")
@CrossOrigin(origins = "http://localhost:4200")
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

    @PostMapping("/simulate")
    public void simulateToday() {
        leagueService.simulateTodayMatches();
    }

    @PostMapping("/generate-fixtures")
    public ResponseEntity<Map<String, String>> generate() {
        fixtureGenerator.generateSeason();
        return ResponseEntity.ok(Map.of("message", "Fixtures Generated!"));
    }

    @GetMapping("/standings")
    public List<Team> getStandings() {
        return teamRepo.findAll().stream()
                .sorted((t1, t2) -> Integer.compare(t2.getPoints(), t1.getPoints()))
                .toList();
    }

    @GetMapping("/fixtures")
    public List<Fixture> getFixtures() {
        return fixtureRepo.findAll();
    }

    @GetMapping("/clubs")
    public List<Team> getClubs() {
        return teamRepo.findAll().stream()
                .sorted((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName()))
                .toList();
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
    public ResponseEntity<Map<String, String>> syncTeams() {
        externalApiService.fetchTeamsFromApi();
        return ResponseEntity.ok(Map.of("message", "Sync started."));
    }

    @PostMapping("/sync-standings")
    public ResponseEntity<Void> syncStandings() {
        // Priority: Always fetch official standings to populate Pl, W, D, L, GD, Pts
        externalApiService.fetchOfficialStandings();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync-real-fixtures")
    public ResponseEntity<Map<String, String>> syncRealFixtures() {
        externalApiService.fetchRealFixtures();
        return ResponseEntity.ok(Map.of("message", "Real Fixtures sync started"));
    }

    @PostMapping("/sync-injuries")
    public ResponseEntity<Map<String, String>> syncInjuries() {
        externalApiService.fetchInjuries();
        return ResponseEntity.ok(Map.of("message", "Injuries sync started"));
    }

    @PostMapping("/sync-official-standings")
    public ResponseEntity<Map<String, String>> syncOfficialStandings() {
        externalApiService.fetchOfficialStandings();
        return ResponseEntity.ok(Map.of("message", "Official Standings sync started"));
    }

    @PostMapping("/sync-topscorers")
    public ResponseEntity<Map<String, String>> syncTopScorers() {
        externalApiService.fetchTopScorers();
        return ResponseEntity.ok(Map.of("message", "Top Scorers sync started"));
    }
}