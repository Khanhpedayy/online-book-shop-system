package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.repo.StaffCustomerRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StaffCustomerController {

    private final StaffCustomerRepository repo;

    public StaffCustomerController(StaffCustomerRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/staff/customers")
    public String lookup(@RequestParam(value = "q", required = false) String q, Model model) {
        model.addAttribute("q", q);
        model.addAttribute("rows", repo.search(q, 200));
        return "staff/customer-lookup";
    }
}