package com.collegefest.management.web;

import com.collegefest.management.entity.Admin;
import com.collegefest.management.entity.Judge;
import com.collegefest.management.entity.Participant;
import com.collegefest.management.exception.InvalidCredentialsException;
import com.collegefest.management.service.AdminService;
import com.collegefest.management.service.JudgeService;
import com.collegefest.management.service.ParticipantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthWebController {

    private final AdminService adminService;
    private final ParticipantService participantService;
    private final JudgeService judgeService;

    @Autowired
    public AuthWebController(AdminService adminService,
                              ParticipantService participantService,
                              JudgeService judgeService) {
        this.adminService = adminService;
        this.participantService = participantService;
        this.judgeService = judgeService;
    }

    @GetMapping("/")
    public String root(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if ("ADMIN".equals(role) || session.getAttribute("admin") != null) {
            return "redirect:/dashboard";
        }
        if ("PARTICIPANT".equals(role)) {
            return "redirect:/portal/dashboard";
        }
        if ("JUDGE".equals(role)) {
            return "redirect:/judge/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if ("ADMIN".equals(role) || session.getAttribute("admin") != null) {
            return "redirect:/dashboard";
        }
        if ("PARTICIPANT".equals(role)) {
            return "redirect:/portal/dashboard";
        }
        if ("JUDGE".equals(role)) {
            return "redirect:/judge/dashboard";
        }
        model.addAttribute("pageTitle", "Login");
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(defaultValue = "ADMIN") String role,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        try {
            HttpSession session = request.getSession(true);
            if ("PARTICIPANT".equalsIgnoreCase(role)) {
                Participant participant = participantService.authenticate(username, password);
                session.setAttribute("role", "PARTICIPANT");
                session.setAttribute("userId", participant.getId());
                session.setAttribute("userName", participant.getName());
                return "redirect:/portal/dashboard";
            } else if ("JUDGE".equalsIgnoreCase(role)) {
                Judge judge = judgeService.authenticate(username, password);
                session.setAttribute("role", "JUDGE");
                session.setAttribute("userId", judge.getId());
                session.setAttribute("userName", judge.getName());
                return "redirect:/judge/dashboard";
            } else {
                Admin admin = adminService.authenticate(username, password);
                session.setAttribute("admin", admin.getUsername());
                session.setAttribute("role", "ADMIN");
                session.setAttribute("userName", admin.getFullName() != null ? admin.getFullName() : admin.getUsername());
                return "redirect:/dashboard";
            }
        } catch (InvalidCredentialsException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("selectedRole", role.toUpperCase());
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
