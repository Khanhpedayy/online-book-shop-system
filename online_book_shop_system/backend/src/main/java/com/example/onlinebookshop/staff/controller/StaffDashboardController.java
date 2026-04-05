package com.example.onlinebookshop.staff.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaffDashboardController {

    @GetMapping("/staff/dashboard")
    public String dashboard() {
        return "redirect:/staff/workspace/dashboard";
    }
}