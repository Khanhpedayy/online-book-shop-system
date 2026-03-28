package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.service.StaffShippingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/orders")
public class StaffShippingController {

    private final StaffShippingService service;

    public StaffShippingController(StaffShippingService service) {
        this.service = service;
    }

    @GetMapping("/{orderId}/ship")
    public String shipScreen(@PathVariable long orderId,
                             Model model,
                             RedirectAttributes ra) {
        try {
            model.addAttribute("v", service.getShippingView(orderId));
            return "staff/ship";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/staff/orders/" + orderId;
        }
    }

    @PostMapping("/{orderId}/ship/confirm")
    public String confirmShip(@PathVariable long orderId,
                              @RequestParam(value = "carrier", required = false) String carrier,
                              @RequestParam(value = "trackingCode", required = false) String trackingCode,
                              RedirectAttributes ra) {
        try {
            service.confirmShipped(orderId, carrier, trackingCode);
            ra.addFlashAttribute("successMsg", "Đã xác nhận SHIPPED thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/orders/" + orderId + "/ship";
    }
}