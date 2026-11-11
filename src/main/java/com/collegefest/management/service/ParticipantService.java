package com.collegefest.management.service;

import com.collegefest.management.entity.Participant;
import com.collegefest.management.entity.Team;
import com.collegefest.management.exception.DuplicateResourceException;
import com.collegefest.management.exception.InvalidCredentialsException;
import com.collegefest.management.exception.ResourceNotFoundException;
import com.collegefest.management.repository.ParticipantRepository;
import com.collegefest.management.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final TeamService teamService;

    @Autowired
    public ParticipantService(ParticipantRepository participantRepository, TeamService teamService) {
        this.participantRepository = participantRepository;
        this.teamService = teamService;
    }

    public List<Participant> getAllParticipants() {
        return participantRepository.findAll();
    }

    public Participant getParticipantById(Long id) {
        return participantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found with id: " + id));
    }

    public List<Participant> getParticipantsByTeam(Long teamId) {
        return participantRepository.findByTeamId(teamId);
    }

    @Transactional
    public Participant addParticipant(Participant participant, Long teamId) {
        Team team = teamService.getTeamById(teamId);

        // Business rule: cannot exceed the event's max team size
        teamService.assertCanAddParticipant(team);

        // Prevent leader from being added twice
        if (isLeaderDuplicate(team, participant)) {
            throw new DuplicateResourceException(
                    "Team leader \"" + team.getLeaderName() + "\" is already registered as Participant #1 for this team");
        }

        // Prevent duplicate participant with the same name or email in the same team
        if (isDuplicateInTeam(team, participant)) {
            throw new DuplicateResourceException(
                    "A participant with this name or email is already registered in team \"" + team.getTeamName() + "\"");
        }

        participant.setTeam(team);
        return participantRepository.save(participant);
    }

    @Transactional
    public Participant updateParticipant(Long id, Participant updated) {
        Participant existing = getParticipantById(id);
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setCollege(updated.getCollege());
        return participantRepository.save(existing);
    }

    @Transactional
    public void deleteParticipant(Long id) {
        Participant existing = getParticipantById(id);
        participantRepository.delete(existing);
    }

    public Participant authenticate(String username, String password) {
        Participant participant = participantRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
        if (participant.getPasswordHash() == null || !PasswordUtil.matches(password, participant.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        return participant;
    }

    @Transactional
    public void setCredentials(Long participantId, String username, String rawPassword) {
        Participant participant = getParticipantById(participantId);
        if (participantRepository.existsByUsername(username)) {
            Participant existing = participantRepository.findByUsername(username).orElse(null);
            if (existing != null && !existing.getId().equals(participantId)) {
                throw new DuplicateResourceException("Username '" + username + "' is already taken");
            }
        }
        participant.setUsername(username);
        participant.setPasswordHash(PasswordUtil.hash(rawPassword));
        participantRepository.save(participant);
    }

    private boolean isLeaderDuplicate(Team team, Participant participant) {
        if (team.getLeaderName() != null && participant.getName() != null
                && team.getLeaderName().trim().equalsIgnoreCase(participant.getName().trim())) {
            return true;
        }
        if (team.getLeaderEmail() != null && !team.getLeaderEmail().trim().isEmpty()
                && participant.getEmail() != null && !participant.getEmail().trim().isEmpty()
                && team.getLeaderEmail().trim().equalsIgnoreCase(participant.getEmail().trim())) {
            return true;
        }
        return false;
    }

    private boolean isDuplicateInTeam(Team team, Participant participant) {
        if (team.getParticipants() == null || team.getParticipants().isEmpty()) {
            return false;
        }
        for (Participant existing : team.getParticipants()) {
            if (existing.getId() != null && existing.getId().equals(participant.getId())) {
                continue;
            }
            if (existing.getName() != null && participant.getName() != null
                    && existing.getName().trim().equalsIgnoreCase(participant.getName().trim())) {
                return true;
            }
            if (existing.getEmail() != null && !existing.getEmail().trim().isEmpty()
                    && participant.getEmail() != null && !participant.getEmail().trim().isEmpty()
                    && existing.getEmail().trim().equalsIgnoreCase(participant.getEmail().trim())) {
                return true;
            }
        }
        return false;
    }
}
