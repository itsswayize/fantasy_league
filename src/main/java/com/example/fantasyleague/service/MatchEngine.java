package com.example.fantasyleague.service;

import com.example.fantasyleague.model.Fixture;
import com.example.fantasyleague.model.Team;
import org.springframework.stereotype.Service;

@Service
public class MatchEngine {

    public void simulateFixture(Fixture fixture) {
        Team home = fixture.getHomeTeam();
        Team away = fixture.getAwayTeam();

        // Calculate goals based on Attack vs Defense
        int homeGoals = calculateGoals(home.getAttackRating(), away.getDefenseRating());
        int awayGoals = calculateGoals(away.getAttackRating(), home.getDefenseRating());

        fixture.setHomeScore(homeGoals);
        fixture.setAwayScore(awayGoals);
        fixture.setPlayed(true);

        // Update League Points
        updatePoints(home, away, homeGoals, awayGoals);
    }

    private int calculateGoals(int attack, int defense) {
        // Higher attack vs lower defense = higher goal probability
        double goalChance = (attack - defense + 50) / 100.0;
        int goals = 0;

        // Simulate 10 "scoring opportunities" per match
        for (int i = 0; i < 10; i++) {
            if (Math.random() < (goalChance * 0.15)) {
                goals++;
            }
        }
        return goals;
    }

    private void updatePoints(Team home, Team away, int hScore, int aScore) {
        if (hScore > aScore) {
            home.setPoints(home.getPoints() + 3);
        } else if (aScore > hScore) {
            away.setPoints(away.getPoints() + 3);
        } else {
            home.setPoints(home.getPoints() + 1);
            away.setPoints(away.getPoints() + 1);
        }
    }
}