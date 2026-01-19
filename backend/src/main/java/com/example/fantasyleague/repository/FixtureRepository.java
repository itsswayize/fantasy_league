package com.example.fantasyleague.repository;

import com.example.fantasyleague.model.Fixture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FixtureRepository extends JpaRepository<Fixture, Long> {
    List<Fixture> findByMatchDateAndPlayedFalse(LocalDate date);
}