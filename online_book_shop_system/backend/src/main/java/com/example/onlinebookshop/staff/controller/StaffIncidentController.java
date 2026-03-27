package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.repo.AuditLogRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/incidents")
public class StaffIncidentController {

    private final AuditLogRepository audit;

    public StaffIncidentController(AuditLogRepository audit) {
        this.audit = audit;
    }

    @GetMapping
    public String form() {
        return "redirect:/staff/workspace/dashboard";
    }

    @PostMapping
    public String submit(@RequestParam("entityTable") String entityTable,
                         @RequestParam(value = "entityId", required = false) Long entityId,
                         @RequestParam("note") String note,
                         RedirectAttributes ra) {
        try {
            audit.log(null,
                    "INCIDENT_REPORT",
                    entityTable == null ? "unknown" : entityTable.trim(),
                    entityId,
                    null,
                    note == null ? null : note.trim());

            ra.addFlashAttribute("successMsg", "Đã ghi nhận incident.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/workspace/dashboard";
    }
}