package com.collegefest.management.service;

import com.collegefest.management.entity.Event;
import com.collegefest.management.entity.Judge;
import com.collegefest.management.exception.DuplicateResourceException;
import com.collegefest.management.exception.InvalidCredentialsException;
import com.collegefest.management.exception.ResourceNotFoundException;
import com.collegefest.management.repository.EventRepository;
import com.collegefest.management.repository.JudgeRepository;
import com.collegefest.management.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JudgeService {

    private final JudgeRepository judgeRepository;
    private final EventRepository eventRepository;

    @Autowired
    public JudgeService(JudgeRepository judgeRepository, EventRepository eventRepository) {
        this.judgeRepository = judgeRepository;
        this.eventRepository = eventRepository;
    }

    public List<Judge> getAllJudges() {
        return judgeRepository.findAll();
    }

    public Judge getJudgeById(Long id) {
        return judgeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Judge not found with id: " + id));
    }

    @Transactional
    public Judge createJudge(Judge judge) {
        if (judgeRepository.existsByEmailIgnoreCase(judge.getEmail())) {
            throw new DuplicateResourceException("A judge with email " + judge.getEmail() + " already exists");
        }
        return judgeRepository.save(judge);
    }

    @Transactional
    public Judge updateJudge(Long id, Judge updated) {
        Judge existing = getJudgeById(id);

        judgeRepository.findByEmailIgnoreCase(updated.getEmail()).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new DuplicateResourceException("A judge with email " + updated.getEmail() + " already exists");
            }
        });

        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setExpertise(updated.getExpertise());
        return judgeRepository.save(existing);
    }

    @Transactional
    public void deleteJudge(Long id) {
        Judge existing = getJudgeById(id);
        judgeRepository.delete(existing);
    }

    public Judge authenticate(String username, String password) {
        Judge judge = judgeRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
        if (judge.getPasswordHash() == null || !PasswordUtil.matches(password, judge.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        return judge;
    }

    @Transactional
    public void assignEvent(Long judgeId, Long eventId) {
        Judge judge = getJudgeById(judgeId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        if (!judge.getAssignedEvents().contains(event)) {
            judge.getAssignedEvents().add(event);
            judgeRepository.save(judge);
        }
    }

    @Transactional
    public void unassignEvent(Long judgeId, Long eventId) {
        Judge judge = getJudgeById(judgeId);
        judge.getAssignedEvents().removeIf(e -> e.getId().equals(eventId));
        judgeRepository.save(judge);
    }

    @Transactional
    public void setCredentials(Long judgeId, String username, String rawPassword) {
        Judge judge = getJudgeById(judgeId);
        if (judgeRepository.existsByUsername(username)) {
            Judge existing = judgeRepository.findByUsername(username).orElse(null);
            if (existing != null && !existing.getId().equals(judgeId)) {
                throw new DuplicateResourceException("Username '" + username + "' is already taken");
            }
        }
        judge.setUsername(username);
        judge.setPasswordHash(PasswordUtil.hash(rawPassword));
        judgeRepository.save(judge);
    }
}
