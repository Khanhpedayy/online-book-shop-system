package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.service.StaffOrderService;
import com.example.onlinebookshop.staff.service.StaffDashboardStats;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;

@Controller
public class StaffDashboardController {

    private final StaffOrderService staffOrderService;

    public StaffDashboardController(StaffOrderService staffOrderService) {
        this.staffOrderService = staffOrderService;
    }

    @GetMapping("/staff/dashboard")
    public String dashboard(Model model, Authentication authentication) {

        String username = (authentication != null) ? authentication.getName() : "STAFF";
        model.addAttribute("role", "STAFF");
        model.addAttribute("username", username);

        // Default values để không bao giờ 500
        model.addAttribute("stats", new StaffDashboardStats(0, 0, 0, 0, 0));
        model.addAttribute("todoOrders", Collections.emptyList());
        model.addAttribute("dbError", null);

        model.addAttribute("alerts", Collections.emptyList());

        try {
            model.addAttribute("stats", staffOrderService.getDashboardStats());
            model.addAttribute("todoOrders", staffOrderService.getTodoList());
            model.addAttribute("alerts", staffOrderService.getAlerts());
        } catch (Exception ex) {
            // hiển thị lỗi gọn gàng trên UI để bạn biết DB đang fail chỗ nào
            model.addAttribute("dbError", ex.getMessage());
        }

        return "redirect:/staff/workspace/dashboard";
    }
}