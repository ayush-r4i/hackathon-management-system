package com.collegefest.management.repository;

import com.collegefest.management.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByTeamId(Long teamId);
    Optional<Participant> findByUsername(String username);
    boolean existsByUsername(String username);
}
