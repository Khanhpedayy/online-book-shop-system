package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.service.StaffDeliveryService;
import com.example.onlinebookshop.staff.service.StaffPackingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaffDeliveryPageController {

    private final StaffPackingService packingService;
    private final StaffDeliveryService deliveryService;

    public StaffDeliveryPageController(StaffPackingService packingService,
                                       StaffDeliveryService deliveryService) {
        this.packingService = packingService;
        this.deliveryService = deliveryService;
    }

    @GetMapping("/staff/workspace/packing")
    public String packingQueue(
            @org.springframework.web.bind.annotation.RequestParam(name = "q", required = false, defaultValue = "") String q,
            @org.springframework.web.bind.annotation.RequestParam(name = "status", required = false, defaultValue = "") String status,
            Model model) {
        model.addAttribute("rows", packingService.getPackingQueue(q, status));
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        return "staff/workspace-packing";
    }

    @GetMapping("/staff/workspace/shipping")
    public String shippingQueue(
            @org.springframework.web.bind.annotation.RequestParam(name = "q", required = false, defaultValue = "") String q,
            @org.springframework.web.bind.annotation.RequestParam(name = "status", required = false, defaultValue = "") String status,
            Model model) {
        model.addAttribute("rows", deliveryService.getShippingQueue(q, status));
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        return "staff/workspace-shipping";
    }

//    @GetMapping("/staff/workspace/delivery-return")
//    public String deliveryReturnRedirect() {
//        return "redirect:/staff/workspace/shipping";
//    }
}