package com.collegefest.management.web;

import com.collegefest.management.entity.Event;
import com.collegefest.management.entity.Participant;
import com.collegefest.management.entity.Team;
import com.collegefest.management.service.EventService;
import com.collegefest.management.service.ParticipantService;
import com.collegefest.management.service.ScoreService;
import com.collegefest.management.service.TeamService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/portal")
public class ParticipantPortalController {

    private final ParticipantService participantService;
    private final EventService eventService;
    private final TeamService teamService;
    private final ScoreService scoreService;

    @Autowired
    public ParticipantPortalController(ParticipantService participantService,
                                        EventService eventService,
                                        TeamService teamService,
                                        ScoreService scoreService) {
        this.participantService = participantService;
        this.eventService = eventService;
        this.teamService = teamService;
        this.scoreService = scoreService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        Participant participant = participantService.getParticipantById(userId);
        Team team = participant.getTeam();

        model.addAttribute("pageTitle", "Participant Dashboard");
        model.addAttribute("participant", participant);
        model.addAttribute("team", team);
        model.addAttribute("userName", session.getAttribute("userName"));

        if (team != null && team.getEvent() != null) {
            model.addAttribute("leaderboard", scoreService.getLeaderboardByEventId(team.getEvent().getId()));
            model.addAttribute("eventName", team.getEvent().getName());
        } else {
            model.addAttribute("leaderboard", Collections.emptyList());
            model.addAttribute("eventName", "N/A");
        }

        return "portal/dashboard";
    }

    @GetMapping("/events")
    public String browseEvents(HttpSession session, Model model) {
        model.addAttribute("pageTitle", "Browse Events");
        model.addAttribute("events", eventService.getAllEvents());
        model.addAttribute("userName", session.getAttribute("userName"));
        return "portal/events";
    }

    @GetMapping("/team")
    public String myTeam(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        Participant participant = participantService.getParticipantById(userId);
        Team team = participant.getTeam();

        model.addAttribute("pageTitle", "My Team");
        model.addAttribute("participant", participant);
        model.addAttribute("team", team);
        model.addAttribute("members", team != null ? participantService.getParticipantsByTeam(team.getId()) : Collections.emptyList());
        model.addAttribute("userName", session.getAttribute("userName"));
        return "portal/team";
    }

    @GetMapping("/team/new")
    public String newTeamForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        Participant participant = participantService.getParticipantById(userId);

        if (participant.getTeam() != null) {
            return "redirect:/portal/team";
        }

        model.addAttribute("pageTitle", "Register Team");
        model.addAttribute("team", new Team());
        model.addAttribute("participant", participant);
        model.addAttribute("events", eventService.getAllEvents());
        model.addAttribute("userName", session.getAttribute("userName"));
        return "portal/team-form";
    }

    @PostMapping("/team/register")
    public String registerTeam(@RequestParam Long eventId,
                               @ModelAttribute Team team,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        Participant participant = participantService.getParticipantById(userId);

        if (participant.getTeam() != null) {
            redirectAttributes.addFlashAttribute("error", "You are already in a team.");
            return "redirect:/portal/team";
        }

        try {
            // Pre-fill leader information from logged-in participant
            team.setLeaderName(participant.getName());
            team.setLeaderEmail(participant.getEmail());
            team.setLeaderPhone(participant.getPhone());

            Team saved = teamService.registerTeam(team, eventId);

            // Re-fetch leader participant created by registerTeam and update with username/hash
            List<Participant> members = participantService.getParticipantsByTeam(saved.getId());
            if (!members.isEmpty()) {
                Participant leader = members.get(0);
                leader.setUsername(participant.getUsername());
                leader.setPasswordHash(participant.getPasswordHash());
                // Update session userId to point to this leader participant if distinct
                session.setAttribute("userId", leader.getId());
            }

            redirectAttributes.addFlashAttribute("success", "Team registered successfully!");
            return "redirect:/portal/team";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/portal/team/new";
        }
    }

    @PostMapping("/team/members/add")
    public String addMember(@RequestParam String name,
                            @RequestParam(required = false) String email,
                            @RequestParam(required = false) String phone,
                            @RequestParam(required = false) String college,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        Participant participant = participantService.getParticipantById(userId);
        Team team = participant.getTeam();

        if (team == null) {
            redirectAttributes.addFlashAttribute("error", "You do not belong to a team.");
            return "redirect:/portal/team";
        }

        try {
            Participant newMember = new Participant();
            newMember.setName(name);
            newMember.setEmail(email);
            newMember.setPhone(phone);
            newMember.setCollege(college);

            participantService.addParticipant(newMember, team.getId());
            redirectAttributes.addFlashAttribute("success", "Member added successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/portal/team";
    }
}
