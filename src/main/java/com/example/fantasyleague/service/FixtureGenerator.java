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
    private final FixtureGenerator fixtureGenerator;

    public FixtureGenerator(TeamRepository teamRepo, FixtureRepository fixtureRepo, FixtureGenerator fixtureGenerator) {
        this.teamRepo = teamRepo;
        this.fixtureRepo = fixtureRepo;
        this.fixtureGenerator = fixtureGenerator;
    }

    public void generateSeason() {
        List<Team> allTeams = teamRepo.findAll();
        LocalDate startDate = LocalDate.now();

        // Simple Round Robin: Everyone plays everyone else once
        for (int i = 0; i < allTeams.size(); i++) {
            for (int j = i + 1; j < allTeams.size(); j++) {
                Fixture f = new Fixture();
                f.setHomeTeam(allTeams.get(i));
                f.setAwayTeam(allTeams.get(j));
                f.setMatchDate(startDate); // For now, let's put them all on the same day to test
                f.setPlayed(false);
                fixtureRepo.save(f);
            }
        }
    }
}