package com.example.fantasyleague.controller;

import com.example.fantasyleague.model.Fixture;
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
    // 1. Declare the variable here
    private final ExternalApiService externalApiService; // Add this field

    public LeagueController(LeagueService leagueService,
                            TeamRepository teamRepo,
                            FixtureRepository fixtureRepo,
                            FixtureGenerator fixtureGenerator,
                            ExternalApiService externalApiService) { // Add this parameter
        this.leagueService = leagueService;
        this.teamRepo = teamRepo;
        this.fixtureRepo = fixtureRepo;
        this.fixtureGenerator = fixtureGenerator;
        this.externalApiService = externalApiService; // Assign it
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

    @PostMapping("/sync-teams")
    public ResponseEntity<Map<String, String>> syncTeams() {
        externalApiService.fetchTeamsFromApi();
        return ResponseEntity.ok(Map.of("message", "Sync started. Check console for completion."));
    }
}