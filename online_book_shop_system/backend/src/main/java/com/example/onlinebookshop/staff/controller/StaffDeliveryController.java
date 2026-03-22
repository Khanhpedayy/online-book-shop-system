package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.service.StaffDeliveryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/orders")
public class StaffDeliveryController {

    private final StaffDeliveryService service;

    public StaffDeliveryController(StaffDeliveryService service) {
        this.service = service;
    }

    @PostMapping("/{orderId}/delivery-outcome")
    public String setOutcome(@PathVariable long orderId,
                             @RequestParam("outcome") String outcome,
                             @RequestParam(value = "reason", required = false) String reason,
                             RedirectAttributes ra) {
        try {
            var rs = service.setDeliveryOutcome(orderId, outcome, reason);
            ra.addFlashAttribute("successMsg", rs.message());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/orders/" + orderId + "/delivery";
    }
}