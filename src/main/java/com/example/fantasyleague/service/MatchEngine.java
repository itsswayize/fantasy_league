package com.example.fantasyleague.service;

import com.example.fantasyleague.model.Fixture;
import com.example.fantasyleague.model.Team;
import org.springframework.stereotype.Service;

@Service
public class MatchEngine {

    public void simulateFixture(Fixture fixture) {
        Team home = fixture.getHomeTeam();
        Team away = fixture.getAwayTeam();

        int homeGoals = calculateGoals(home.getAttackRating(), away.getDefenseRating());
        int awayGoals = calculateGoals(away.getAttackRating(), home.getDefenseRating());

        fixture.setHomeScore(homeGoals);
        fixture.setAwayScore(awayGoals);
        fixture.setPlayed(true);

        // REMOVED: updatePoints(home, away, homeGoals, awayGoals);
        // This ensures official standings from the API are not overwritten by random data.
    }

    private int calculateGoals(int attack, int defense) {
        double goalChance = (attack - defense + 50) / 100.0;
        int goals = 0;
        for (int i = 0; i < 10; i++) {
            if (Math.random() < (goalChance * 0.15)) {
                goals++;
            }
        }
        return goals;
    }
}