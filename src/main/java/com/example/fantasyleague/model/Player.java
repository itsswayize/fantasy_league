package com.example.fantasyleague.model;

import com.fasterxml.jackson.annotation.JsonIgnore; // Add this import
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

    @ManyToOne
    @JoinColumn(name = "team_id")
    @JsonIgnore // This stops the infinite loop
    private Team team;
}