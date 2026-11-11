package com.collegefest.management.web;

import com.collegefest.management.entity.Event;
import com.collegefest.management.entity.Judge;
import com.collegefest.management.entity.Score;
import com.collegefest.management.entity.Team;
import com.collegefest.management.service.EventService;
import com.collegefest.management.service.JudgeService;
import com.collegefest.management.service.ScoreService;
import com.collegefest.management.service.TeamService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/judge")
public class JudgePortalController {

    private final JudgeService judgeService;
    private final EventService eventService;
    private final TeamService teamService;
    private final ScoreService scoreService;

    @Autowired
    public JudgePortalController(JudgeService judgeService,
                                  EventService eventService,
                                  TeamService teamService,
                                  ScoreService scoreService) {
        this.judgeService = judgeService;
        this.eventService = eventService;
        this.teamService = teamService;
        this.scoreService = scoreService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long judgeId = (Long) session.getAttribute("userId");
        Judge judge = judgeService.getJudgeById(judgeId);

        model.addAttribute("pageTitle", "Judge Dashboard");
        model.addAttribute("judge", judge);
        model.addAttribute("assignedEvents", judge.getAssignedEvents());
        model.addAttribute("userName", session.getAttribute("userName"));
        return "judge/dashboard";
    }

    @GetMapping("/scores")
    public String scores(@RequestParam(required = false) Long eventId,
                          HttpSession session, Model model) {
        Long judgeId = (Long) session.getAttribute("userId");
        Judge judge = judgeService.getJudgeById(judgeId);
        List<Event> assignedEvents = judge.getAssignedEvents();

        model.addAttribute("pageTitle", "Score Teams");
        model.addAttribute("assignedEvents", assignedEvents);
        model.addAttribute("selectedEventId", eventId);
        model.addAttribute("userName", session.getAttribute("userName"));

        Map<Long, Score> scoreByTeamId = new HashMap<>();

        if (eventId != null) {
            boolean isAssigned = assignedEvents.stream().anyMatch(e -> e.getId().equals(eventId));
            if (!isAssigned) {
                model.addAttribute("error", "You are not assigned to evaluate this event.");
                model.addAttribute("teams", List.of());
            } else {
                List<Team> teams = teamService.getTeamsByEvent(eventId);
                model.addAttribute("teams", teams);

                for (Score s : scoreService.getAllScores()) {
                    if (s.getJudge().getId().equals(judgeId) && s.getTeam().getEvent() != null
                            && s.getTeam().getEvent().getId().equals(eventId)) {
                        scoreByTeamId.put(s.getTeam().getId(), s);
                    }
                }
            }
        } else {
            model.addAttribute("teams", List.of());
        }

        model.addAttribute("scoreByTeamId", scoreByTeamId);
        return "judge/scores";
    }

    @GetMapping("/scores/new")
    public String newScoreForm(@RequestParam Long teamId,
                                HttpSession session, Model model) {
        Long judgeId = (Long) session.getAttribute("userId");
        Judge judge = judgeService.getJudgeById(judgeId);
        Team team = teamService.getTeamById(teamId);

        boolean isAssigned = judge.getAssignedEvents().stream()
                .anyMatch(e -> team.getEvent() != null && e.getId().equals(team.getEvent().getId()));
        if (!isAssigned) {
            return "redirect:/judge/scores";
        }

        Score existing = scoreService.getAllScores().stream()
                .filter(s -> s.getTeam().getId().equals(teamId) && s.getJudge().getId().equals(judgeId))
                .findFirst().orElse(null);

        if (existing != null) {
            return "redirect:/judge/scores/edit?scoreId=" + existing.getId();
        }

        model.addAttribute("pageTitle", "Score Team: " + team.getTeamName());
        model.addAttribute("team", team);
        model.addAttribute("score", new Score());
        model.addAttribute("judgeId", judgeId);
        model.addAttribute("userName", session.getAttribute("userName"));
        return "judge/score-form";
    }

    @PostMapping("/scores/submit")
    public String submitScore(@RequestParam Long teamId,
                               @ModelAttribute Score score,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long judgeId = (Long) session.getAttribute("userId");
        Judge judge = judgeService.getJudgeById(judgeId);
        Team team = teamService.getTeamById(teamId);

        boolean isAssigned = judge.getAssignedEvents().stream()
                .anyMatch(e -> team.getEvent() != null && e.getId().equals(team.getEvent().getId()));
        if (!isAssigned) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to score this team.");
            return "redirect:/judge/scores";
        }

        try {
            scoreService.submitScore(score, teamId, judgeId);
            redirectAttributes.addFlashAttribute("success", "Score submitted successfully!");
            Long eventId = team.getEvent() != null ? team.getEvent().getId() : null;
            return "redirect:/judge/scores" + (eventId != null ? "?eventId=" + eventId : "");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/judge/scores/new?teamId=" + teamId;
        }
    }

    @GetMapping("/scores/edit")
    public String editScoreForm(@RequestParam Long scoreId,
                                 HttpSession session, Model model) {
        Long judgeId = (Long) session.getAttribute("userId");
        Score score = scoreService.getScoreById(scoreId);

        if (!score.getJudge().getId().equals(judgeId)) {
            return "redirect:/judge/scores";
        }

        model.addAttribute("pageTitle", "Edit Score: " + score.getTeam().getTeamName());
        model.addAttribute("team", score.getTeam());
        model.addAttribute("score", score);
        model.addAttribute("judgeId", judgeId);
        model.addAttribute("isEdit", true);
        model.addAttribute("userName", session.getAttribute("userName"));
        return "judge/score-form";
    }
}
