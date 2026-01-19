package com.example.fantasyleague.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String position;
    private String imageUrl;
    private boolean injured;
    private String injuryType;

    // Add these two fields to enable setGoals and setAssists
    private int goals;
    private int assists;

    @ManyToOne
    @JoinColumn(name = "team_id")
    @JsonIgnore
    private Team team;
}