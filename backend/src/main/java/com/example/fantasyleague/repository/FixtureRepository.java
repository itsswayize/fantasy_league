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

    // Used by your getFixtures API to filter by date range
    @Query("SELECT f FROM Fixture f WHERE f.matchDate BETWEEN :startDate AND :endDate ORDER BY f.matchDate ASC, f.matchTime ASC")
    List<Fixture> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // CRITICAL FIX: This was missing and caused the build failure in LeagueService
    List<Fixture> findByMatchDateAndPlayedFalse(LocalDate matchDate);
}