package com.example.fantasyleague.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Getter
@Setter
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String logoUrl; // For realistic club logos

    // League Table Fields
    private int matchesPlayed; // Pl
    private int wins;          // W
    private int draws;         // D
    private int losses;        // L
    private int goalsFor;      // GF
    private int goalsAgainst;  // GA
    private int goalDifference;// GD
    private int points;        // Pts

    // Existing ratings
    private int attackRating;
    private int defenseRating;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Player> squad;
}