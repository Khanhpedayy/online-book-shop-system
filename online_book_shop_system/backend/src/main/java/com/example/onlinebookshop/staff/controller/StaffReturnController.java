package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.service.StaffReturnService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/returns")
public class StaffReturnController {

    private final StaffReturnService service;

    public StaffReturnController(StaffReturnService service) {
        this.service = service;
    }

    // /staff/returns/new?orderId=123
    @GetMapping("/new")
    public String createScreen(@RequestParam("orderId") long orderId, Model model) {
        model.addAttribute("v", service.buildCreateScreen(orderId));
        return "staff/return-create";
    }

    @PostMapping("/create")
    public String create(@RequestParam("orderId") long orderId,
                         @RequestParam(value = "reason", required = false) String reason,
                         @RequestParam(value = "note", required = false) String note,
                         RedirectAttributes ra) {
        try {
            long rid = service.createReturn(orderId, reason, note);
            ra.addFlashAttribute("successMsg", "Đã tạo return intake.");
            return "redirect:/staff/returns/" + rid;
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/staff/returns/new?orderId=" + orderId;
        }
    }

    @GetMapping("/{returnId}")
    public String intake(@PathVariable long returnId, Model model) {
        model.addAttribute("v", service.getIntakeView(returnId));
        return "staff/return-intake";
    }

    @PostMapping("/{returnId}/scan")
    public String scan(@PathVariable long returnId,
                       @RequestParam("orderItemId") long orderItemId,
                       @RequestParam("copyCode") String copyCode,
                       @RequestParam("conditionGrade") String conditionGrade,
                       @RequestParam(value = "conditionNote", required = false) String conditionNote,
                       RedirectAttributes ra) {
        try {
            service.scanReturnedCopy(returnId, orderItemId, copyCode, conditionGrade, conditionNote);
            ra.addFlashAttribute("successMsg", "Scan OK: " + copyCode);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/returns/" + returnId;
    }
}