package com.example.fantasyleague.repository;

import com.example.fantasyleague.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    // Finds exact matches
    Team findByName(String name);

    // FIX: Lets PostgreSQL do the heavy lifting for fuzzy matching!
    Team findFirstByNameContainingIgnoreCase(String name);
}