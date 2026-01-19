package com.example.fantasyleague.service;

import com.example.fantasyleague.model.Fixture;
import com.example.fantasyleague.repository.FixtureRepository;
import com.example.fantasyleague.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeagueService {

    private final FixtureRepository fixtureRepo;
    private final TeamRepository teamRepo;
    private final MatchEngine matchEngine;

    public LeagueService(FixtureRepository fixtureRepo, TeamRepository teamRepo, MatchEngine matchEngine) {
        this.fixtureRepo = fixtureRepo;
        this.teamRepo = teamRepo;
        this.matchEngine = matchEngine;
    }

    @Transactional
    public void simulateTodayMatches() {
        List<Fixture> todayFixtures = fixtureRepo.findByMatchDateAndPlayedFalse(LocalDate.now());

        for (Fixture fixture : todayFixtures) {
            matchEngine.simulateFixture(fixture);
        }

        fixtureRepo.saveAll(todayFixtures);
        // Note: teamRepo.save happens automatically due to @Transactional
    }
}