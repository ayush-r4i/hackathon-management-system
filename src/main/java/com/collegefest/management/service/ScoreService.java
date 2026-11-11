package com.collegefest.management.service;

import com.collegefest.management.dto.LeaderboardEntry;
import com.collegefest.management.entity.Judge;
import com.collegefest.management.entity.Score;
import com.collegefest.management.entity.Team;
import com.collegefest.management.exception.InvalidScoreException;
import com.collegefest.management.exception.ResourceNotFoundException;
import com.collegefest.management.repository.ScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final TeamService teamService;
    private final JudgeService judgeService;

    @Autowired
    public ScoreService(ScoreRepository scoreRepository, TeamService teamService, JudgeService judgeService) {
        this.scoreRepository = scoreRepository;
        this.teamService = teamService;
        this.judgeService = judgeService;
    }

    public List<Score> getAllScores() {
        return scoreRepository.findAll();
    }

    public Score getScoreById(Long id) {
        return scoreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Score not found with id: " + id));
    }

    /**
     * Creates a new score, or updates the existing one if this judge has already
     * scored this team (one score per judge per team).
     */
    @Transactional
    public Score submitScore(Score incoming, Long teamId, Long judgeId) {
        Team team = teamService.getTeamById(teamId);
        Judge judge = judgeService.getJudgeById(judgeId);

        validateScoreValues(incoming);

        Score score = scoreRepository.findByTeamIdAndJudgeId(teamId, judgeId).orElseGet(Score::new);
        score.setTeam(team);
        score.setJudge(judge);
        score.setInnovation(incoming.getInnovation());
        score.setTechnicalImplementation(incoming.getTechnicalImplementation());
        score.setPresentation(incoming.getPresentation());
        score.setUiUx(incoming.getUiUx());
        score.setOverallImpression(incoming.getOverallImpression());
        score.setFinalScore(calculateFinalScore(incoming));

        return scoreRepository.save(score);
    }

    @Transactional
    public void deleteScore(Long id) {
        Score existing = getScoreById(id);
        scoreRepository.delete(existing);
    }

    private void validateScoreValues(Score score) {
        double[] values = {
                nullToZeroCheck(score.getInnovation(), "Innovation"),
                nullToZeroCheck(score.getTechnicalImplementation(), "Technical Implementation"),
                nullToZeroCheck(score.getPresentation(), "Presentation"),
                nullToZeroCheck(score.getUiUx(), "UI/UX"),
                nullToZeroCheck(score.getOverallImpression(), "Overall Impression")
        };
        for (double v : values) {
            if (v < 0.0 || v > 10.0) {
                throw new InvalidScoreException("All score fields must be between 0 and 10");
            }
        }
    }

    private double nullToZeroCheck(Double value, String fieldName) {
        if (value == null) {
            throw new InvalidScoreException(fieldName + " score is required");
        }
        return value;
    }

    /** Final score = average of the five judging criteria, rounded to 2 decimal places. */
    private double calculateFinalScore(Score score) {
        double sum = score.getInnovation() + score.getTechnicalImplementation()
                + score.getPresentation() + score.getUiUx() + score.getOverallImpression();
        double average = sum / 5.0;
        return Math.round(average * 100.0) / 100.0;
    }

    /**
     * Builds the live leaderboard across all events: for every team that has at least one score,
     * average its final scores across all judges, then rank teams highest-first.
     */
    public List<LeaderboardEntry> getLeaderboard() {
        return buildLeaderboardFromScores(scoreRepository.findAll());
    }

    /**
     * Builds the live leaderboard filtered by event: for every team in the specified event
     * that has at least one score, average its final scores across all judges, then rank highest-first.
     */
    public List<LeaderboardEntry> getLeaderboardByEventId(Long eventId) {
        if (eventId == null) {
            return Collections.emptyList();
        }
        return buildLeaderboardFromScores(scoreRepository.findByTeamEventId(eventId));
    }

    private List<LeaderboardEntry> buildLeaderboardFromScores(List<Score> scores) {
        Map<Long, List<Score>> scoresByTeam = scores.stream()
                .collect(Collectors.groupingBy(s -> s.getTeam().getId()));

        List<LeaderboardEntry> entries = scoresByTeam.entrySet().stream()
                .map(entry -> {
                    List<Score> teamScores = entry.getValue();
                    Team team = teamScores.get(0).getTeam();
                    double avg = teamScores.stream().mapToDouble(Score::getFinalScore).average().orElse(0.0);
                    avg = Math.round(avg * 100.0) / 100.0;

                    double avgTech = teamScores.stream()
                            .mapToDouble(s -> s.getTechnicalImplementation() != null ? s.getTechnicalImplementation() : 0.0)
                            .average().orElse(0.0);
                    avgTech = Math.round(avgTech * 100.0) / 100.0;

                    double avgInno = teamScores.stream()
                            .mapToDouble(s -> s.getInnovation() != null ? s.getInnovation() : 0.0)
                            .average().orElse(0.0);
                    avgInno = Math.round(avgInno * 100.0) / 100.0;

                    return new LeaderboardEntry(
                            team.getId(),
                            team.getTeamName(),
                            team.getEvent() != null ? team.getEvent().getName() : "",
                            avg,
                            teamScores.size(),
                            avgTech,
                            avgInno);
                })
                .sorted(Comparator.<LeaderboardEntry>comparingDouble(e -> -e.getAverageScore())
                        .thenComparingDouble(e -> -e.getAvgTechnicalImplementation())
                        .thenComparingDouble(e -> -e.getAvgInnovation()))
                .collect(Collectors.toList());

        // Assign ranks with tie breaking and shared ranks for true ties
        int currentRank = 1;
        for (int i = 0; i < entries.size(); i++) {
            if (i == 0) {
                entries.get(i).setRank(currentRank);
            } else {
                LeaderboardEntry prev = entries.get(i - 1);
                LeaderboardEntry curr = entries.get(i);
                boolean tied = Double.compare(curr.getAverageScore(), prev.getAverageScore()) == 0
                        && Double.compare(curr.getAvgTechnicalImplementation(), prev.getAvgTechnicalImplementation()) == 0
                        && Double.compare(curr.getAvgInnovation(), prev.getAvgInnovation()) == 0;
                if (tied) {
                    curr.setRank(prev.getRank());
                } else {
                    currentRank = i + 1;
                    curr.setRank(currentRank);
                }
            }
        }
        return entries;
    }
}
