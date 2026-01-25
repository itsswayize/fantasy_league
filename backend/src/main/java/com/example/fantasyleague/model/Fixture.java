package com.example.fantasyleague.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Fixture {
    @Id
    // REMOVED @GeneratedValue - We strictly use the API's ID to prevent duplicates
    private Long id;

    @ManyToOne
    private Team homeTeam;

    @ManyToOne
    private Team awayTeam;

    private int homeScore;
    private int awayScore;
    private LocalDate matchDate;

    private String matchTime; // e.g. "15:00"
    private String status;    // e.g. "Finished", "Live"
    private boolean played;
}