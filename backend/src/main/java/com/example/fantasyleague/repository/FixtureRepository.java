package com.example.fantasyleague.repository;

import com.example.fantasyleague.model.Fixture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FixtureRepository extends JpaRepository<Fixture, Long> {

    @Query("SELECT f FROM Fixture f WHERE f.matchDate BETWEEN :startDate AND :endDate ORDER BY f.matchDate ASC, f.matchTime ASC")
    List<Fixture> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Fixture> findByMatchDateAndPlayedFalse(LocalDate matchDate);

    // FIXED: Bulk delete to prevent deadlocks and clear bad data instantly
    @Modifying
    @Transactional
    @Query("DELETE FROM Fixture f WHERE f.matchDate BETWEEN :startDate AND :endDate")
    void deleteByMatchDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}