package com.example.fantasyleague.controller;

import com.example.fantasyleague.model.Fixture;
import com.example.fantasyleague.model.Team;
import com.example.fantasyleague.repository.FixtureRepository;
import com.example.fantasyleague.repository.TeamRepository;
import com.example.fantasyleague.service.FixtureGenerator;
import com.example.fantasyleague.service.LeagueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/league")
@CrossOrigin(origins = "http://localhost:4200") // Allows Angular to connect
public class LeagueController {

    private final LeagueService leagueService;
    private final TeamRepository teamRepo;
    private final FixtureRepository fixtureRepo;
    private final FixtureGenerator fixtureGenerator;


    public LeagueController(LeagueService leagueService, TeamRepository teamRepo, FixtureRepository fixtureRepo, FixtureGenerator fixtureGenerator) {
        this.leagueService = leagueService;
        this.teamRepo = teamRepo;
        this.fixtureRepo = fixtureRepo;
        this.fixtureGenerator = fixtureGenerator;
    }

    // Trigger daily simulation
    @PostMapping("/simulate")
    public void simulateToday() {
        leagueService.simulateTodayMatches();
    }

    @PostMapping("/generate-fixtures")
    public String generate() {
        fixtureGenerator.generateSeason();
        return "Fixtures Generated!";
    }

    // Get the standings (League Table) sorted by points
    @GetMapping("/standings")
    public List<Team> getStandings() {
        return teamRepo.findAll().stream()
                .sorted((t1, t2) -> Integer.compare(t2.getPoints(), t1.getPoints()))
                .toList();
    }

    // Get all fixtures to show results
    @GetMapping("/fixtures")
    public List<Fixture> getFixtures() {
        return fixtureRepo.findAll();
    }
}