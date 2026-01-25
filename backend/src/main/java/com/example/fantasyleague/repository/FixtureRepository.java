package com.example.fantasyleague.repository;

import com.example.fantasyleague.model.Fixture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FixtureRepository extends JpaRepository<Fixture, Long> {

    // Finds matches for a specific range (for the frontend "Matchweek" view)
    @Query("SELECT f FROM Fixture f WHERE f.matchDate BETWEEN :startDate AND :endDate ORDER BY f.matchDate ASC, f.matchTime ASC")
    List<Fixture> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Finds unplayed matches for a specific day (for the Simulator)
    List<Fixture> findByMatchDateAndPlayedFalse(LocalDate matchDate);
}