package com.example.fantasyleague.service;

import com.example.fantasyleague.model.Fixture;
import com.example.fantasyleague.model.Team;
import com.example.fantasyleague.repository.FixtureRepository;
import com.example.fantasyleague.repository.TeamRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class FixtureGenerator {
    private final TeamRepository teamRepo;
    private final FixtureRepository fixtureRepo;

    public FixtureGenerator(TeamRepository teamRepo, FixtureRepository fixtureRepo) {
        this.teamRepo = teamRepo;
        this.fixtureRepo = fixtureRepo;
    }

    public void generateSeason() {
        // 1. Clean the slate so old matches don't repeat
        fixtureRepo.deleteAll();

        List<Team> allTeams = teamRepo.findAll();
        LocalDate startDate = LocalDate.now();

        // 2. Create one fresh set of matches
        for (int i = 0; i < allTeams.size(); i++) {
            for (int j = i + 1; j < allTeams.size(); j++) {
                Fixture f = new Fixture();
                f.setHomeTeam(allTeams.get(i));
                f.setAwayTeam(allTeams.get(j));
                f.setMatchDate(startDate);
                f.setPlayed(false);
                fixtureRepo.save(f);
            }
        }
    }
}