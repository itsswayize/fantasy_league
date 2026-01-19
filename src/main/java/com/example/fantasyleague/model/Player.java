package com.example.fantasyleague.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String position; // e.g., "Forwards", "Midfielders"
    private String imageUrl;
    private int goals;
    private int assists;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
}