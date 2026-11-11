package com.collegefest.management.service;

import com.collegefest.management.entity.Judge;
import com.collegefest.management.entity.Participant;
import com.collegefest.management.exception.InvalidCredentialsException;
import com.collegefest.management.repository.EventRepository;
import com.collegefest.management.repository.JudgeRepository;
import com.collegefest.management.repository.ParticipantRepository;
import com.collegefest.management.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthAndRoleTest {

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private JudgeRepository judgeRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TeamService teamService;

    private ParticipantService participantService;
    private JudgeService judgeService;

    @BeforeEach
    void setUp() {
        participantService = new ParticipantService(participantRepository, teamService);
        judgeService = new JudgeService(judgeRepository, eventRepository);
    }

    @Test
    void testPasswordUtil_HashAndMatches() {
        String raw = "secret123";
        String hashed = PasswordUtil.hash(raw);
        assertNotNull(hashed);
        assertNotEquals(raw, hashed);
        assertTrue(PasswordUtil.matches(raw, hashed));
        assertFalse(PasswordUtil.matches("wrongpass", hashed));
    }

    @Test
    void testParticipantAuth_SuccessAndFailure() {
        Participant p = new Participant();
        p.setId(1L);
        p.setUsername("alice");
        p.setPasswordHash(PasswordUtil.hash("alicePass"));

        when(participantRepository.findByUsername("alice")).thenReturn(Optional.of(p));
        when(participantRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Participant authenticated = participantService.authenticate("alice", "alicePass");
        assertNotNull(authenticated);
        assertEquals("alice", authenticated.getUsername());

        assertThrows(InvalidCredentialsException.class, () -> {
            participantService.authenticate("alice", "wrongPass");
        });

        assertThrows(InvalidCredentialsException.class, () -> {
            participantService.authenticate("unknown", "anyPass");
        });
    }

    @Test
    void testJudgeAuth_SuccessAndFailure() {
        Judge j = new Judge();
        j.setId(2L);
        j.setUsername("judge_bob");
        j.setPasswordHash(PasswordUtil.hash("bobPass"));

        when(judgeRepository.findByUsername("judge_bob")).thenReturn(Optional.of(j));

        Judge authenticated = judgeService.authenticate("judge_bob", "bobPass");
        assertNotNull(authenticated);
        assertEquals("judge_bob", authenticated.getUsername());

        assertThrows(InvalidCredentialsException.class, () -> {
            judgeService.authenticate("judge_bob", "wrongPass");
        });
    }
}
