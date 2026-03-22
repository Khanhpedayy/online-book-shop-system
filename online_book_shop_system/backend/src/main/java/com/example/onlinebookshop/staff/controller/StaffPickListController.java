package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.service.StaffPickListService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/orders")
public class StaffPickListController {

    private final StaffPickListService service;

    public StaffPickListController(StaffPickListService service) {
        this.service = service;
    }

    @GetMapping("/{orderId}/pick-list")
    public String pickList(@PathVariable long orderId, Model model) {
        model.addAttribute("v", service.getPickList(orderId));
        return "staff/pick-list";
    }

    @PostMapping("/{orderId}/pick-list/scan")
    public String scan(@PathVariable long orderId,
                       @RequestParam("copyCode") String copyCode,
                       RedirectAttributes ra) {
        try {
            service.scanConfirmPicked(orderId, copyCode);
            ra.addFlashAttribute("successMsg", "Picked OK: " + copyCode);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/orders/" + orderId + "/pick-list";
    }
}