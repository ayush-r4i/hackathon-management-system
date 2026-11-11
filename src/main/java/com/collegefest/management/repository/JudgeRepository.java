package com.collegefest.management.repository;

import com.collegefest.management.entity.Judge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JudgeRepository extends JpaRepository<Judge, Long> {
    Optional<Judge> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Optional<Judge> findByUsername(String username);
    boolean existsByUsername(String username);
}
