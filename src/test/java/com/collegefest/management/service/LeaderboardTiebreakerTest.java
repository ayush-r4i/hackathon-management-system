package com.collegefest.management.service;

import com.collegefest.management.dto.LeaderboardEntry;
import com.collegefest.management.entity.Event;
import com.collegefest.management.entity.Judge;
import com.collegefest.management.entity.Score;
import com.collegefest.management.entity.Team;
import com.collegefest.management.repository.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LeaderboardTiebreakerTest {

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private TeamService teamService;

    @Mock
    private JudgeService judgeService;

    private ScoreService scoreService;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        scoreService = new ScoreService(scoreRepository, teamService, judgeService);
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setName("Hackathon 2026");
    }

    private Team createTeam(Long id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setTeamName(name);
        team.setEvent(testEvent);
        return team;
    }

    private Score createScore(Team team, Judge judge, double inno, double tech, double pres, double ui, double overall) {
        Score score = new Score();
        score.setTeam(team);
        score.setJudge(judge);
        score.setInnovation(inno);
        score.setTechnicalImplementation(tech);
        score.setPresentation(pres);
        score.setUiUx(ui);
        score.setOverallImpression(overall);
        double avg = (inno + tech + pres + ui + overall) / 5.0;
        score.setFinalScore(Math.round(avg * 100.0) / 100.0);
        return score;
    }

    @Test
    void testLeaderboard_TiebreakerLogic() {
        Judge judge = new Judge();
        judge.setId(1L);

        // Team 1: final = 8.0, tech = 9.0, inno = 7.0
        Team team1 = createTeam(1L, "Team One");
        Score score1 = createScore(team1, judge, 7.0, 9.0, 8.0, 8.0, 8.0);

        // Team 2: final = 8.0, tech = 8.0, inno = 9.0
        Team team2 = createTeam(2L, "Team Two");
        Score score2 = createScore(team2, judge, 9.0, 8.0, 8.0, 8.0, 8.0);

        // Team 3: final = 8.0, tech = 9.0, inno = 7.0 (identical to Team 1)
        Team team3 = createTeam(3L, "Team Three");
        Score score3 = createScore(team3, judge, 7.0, 9.0, 8.0, 8.0, 8.0);

        // Team 4: final = 9.0 (highest overall)
        Team team4 = createTeam(4L, "Team Four");
        Score score4 = createScore(team4, judge, 9.0, 9.0, 9.0, 9.0, 9.0);

        when(scoreRepository.findAll()).thenReturn(List.of(score1, score2, score3, score4));

        List<LeaderboardEntry> leaderboard = scoreService.getLeaderboard();

        assertEquals(4, leaderboard.size());

        // Rank 1: Team 4 (avg = 9.0)
        assertEquals("Team Four", leaderboard.get(0).getTeamName());
        assertEquals(1, leaderboard.get(0).getRank());

        // Team 1 and Team 3 tie on avg (8.0), tech (9.0), inno (7.0) -> both Rank 2
        assertEquals(2, leaderboard.get(1).getRank());
        assertEquals(2, leaderboard.get(2).getRank());
        assertTrue(leaderboard.get(1).getTeamName().equals("Team One") || leaderboard.get(1).getTeamName().equals("Team Three"));
        assertTrue(leaderboard.get(2).getTeamName().equals("Team One") || leaderboard.get(2).getTeamName().equals("Team Three"));

        // Rank 4: Team 2 (avg = 8.0, but tech is 8.0 vs 9.0)
        assertEquals("Team Two", leaderboard.get(3).getTeamName());
        assertEquals(4, leaderboard.get(3).getRank());
    }
}
