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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Team homeTeam;

    @ManyToOne
    private Team awayTeam;

    private int homeScore;
    private int awayScore;
    private LocalDate matchDate;
    private String matchTime; // Add this to store specifically the time (e.g., "15:00")
    private boolean played;

    // Stores "Finished", "Postponed", "20:00", etc.
    private String status;
}