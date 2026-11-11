package com.collegefest.management.web;

import com.collegefest.management.entity.Event;
import com.collegefest.management.entity.Judge;
import com.collegefest.management.service.EventService;
import com.collegefest.management.service.JudgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/judges")
public class AdminJudgeCredentialController {

    private final JudgeService judgeService;
    private final EventService eventService;

    @Autowired
    public AdminJudgeCredentialController(JudgeService judgeService, EventService eventService) {
        this.judgeService = judgeService;
        this.eventService = eventService;
    }

    @GetMapping("/{id}/credentials")
    public String credentialsForm(@PathVariable Long id, Model model) {
        Judge judge = judgeService.getJudgeById(id);
        List<Event> allEvents = eventService.getAllEvents();
        model.addAttribute("pageTitle", "Judge Credentials & Assignments");
        model.addAttribute("judge", judge);
        model.addAttribute("allEvents", allEvents);
        return "admin/judge-credentials";
    }

    @PostMapping("/{id}/credentials")
    public String saveCredentials(@PathVariable Long id,
                                   @RequestParam String username,
                                   @RequestParam String password,
                                   RedirectAttributes redirectAttributes) {
        try {
            judgeService.setCredentials(id, username, password);
            redirectAttributes.addFlashAttribute("success", "Credentials updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/judges/" + id + "/credentials";
    }

    @PostMapping("/{id}/assign-event")
    public String assignEvent(@PathVariable Long id,
                               @RequestParam Long eventId,
                               RedirectAttributes redirectAttributes) {
        try {
            judgeService.assignEvent(id, eventId);
            redirectAttributes.addFlashAttribute("success", "Event assigned successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/judges/" + id + "/credentials";
    }

    @PostMapping("/{id}/unassign-event")
    public String unassignEvent(@PathVariable Long id,
                                 @RequestParam Long eventId,
                                 RedirectAttributes redirectAttributes) {
        judgeService.unassignEvent(id, eventId);
        redirectAttributes.addFlashAttribute("success", "Event unassigned successfully.");
        return "redirect:/admin/judges/" + id + "/credentials";
    }
}
