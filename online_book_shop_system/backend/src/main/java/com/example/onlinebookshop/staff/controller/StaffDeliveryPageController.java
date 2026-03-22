package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.service.StaffOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/staff/orders")
public class StaffDeliveryPageController {

    private final StaffOrderService staffOrderService;

    public StaffDeliveryPageController(StaffOrderService staffOrderService) {
        this.staffOrderService = staffOrderService;
    }

    @GetMapping("/{orderId}/delivery")
    public String deliveryPage(@PathVariable long orderId, Model model) {
        model.addAttribute("o", staffOrderService.getDetail(orderId));
        return "staff/delivery";
    }
}