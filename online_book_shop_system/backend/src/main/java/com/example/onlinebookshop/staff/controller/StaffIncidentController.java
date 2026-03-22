package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.repo.AuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
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
        return "staff/incident-form";
    }

    @PostMapping
    public String submit(@RequestParam("entityTable") String entityTable,
                         @RequestParam(value = "entityId", required = false) Long entityId,
                         @RequestParam("note") String note,
                         Authentication auth,
                         RedirectAttributes ra) {
        try {
            // actorUserId: hiện tại project chưa map userId từ principal -> để null cho an toàn
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
        return "redirect:/staff/incidents";
    }
}