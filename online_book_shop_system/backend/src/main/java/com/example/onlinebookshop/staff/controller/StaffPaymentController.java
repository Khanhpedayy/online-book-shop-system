package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.repo.StaffPaymentQueryRepository;
import com.example.onlinebookshop.staff.service.StaffOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/payments")
public class StaffPaymentController {

    private final StaffPaymentQueryRepository paymentRepo;
    private final StaffOrderService staffOrderService;

    public StaffPaymentController(StaffPaymentQueryRepository paymentRepo, StaffOrderService staffOrderService) {
        this.paymentRepo = paymentRepo;
        this.staffOrderService = staffOrderService;
    }

    @GetMapping
    public String list() {
        return "redirect:/staff/workspace/dashboard";
    }

    @GetMapping("/{paymentId}")
    public String detail(@PathVariable long paymentId) {
        Long orderId = paymentRepo.getOrderIdByPaymentId(paymentId);
        if (orderId == null) {
            return "redirect:/staff/workspace/dashboard";
        }
        return "redirect:/staff/orders/" + orderId;
    }

    @PostMapping("/{paymentId}/recheck")
    public String recheck(@PathVariable long paymentId, RedirectAttributes ra) {
        try {
            long orderId = paymentRepo.getOrderIdByPaymentId(paymentId);
            String pStatus = paymentRepo.getPaymentStatus(paymentId);
            if (pStatus == null) throw new RuntimeException("Payment status is null");

            String upper = pStatus.toUpperCase();
            if ("SUCCEEDED".equals(upper)) {
                staffOrderService.updatePaymentStatus(orderId, "PAID");
            } else if ("REFUNDED".equals(upper)) {
                staffOrderService.updatePaymentStatus(orderId, "REFUNDED");
            } else if ("FAILED".equals(upper) || "EXPIRED".equals(upper) || "CANCELLED".equals(upper)) {
                staffOrderService.updatePaymentStatus(orderId, "FAILED");
            } else {
                staffOrderService.updatePaymentStatus(orderId, "PENDING");
            }

            ra.addFlashAttribute("successMsg", "Đã recheck & sync payment_status cho order " + orderId);
            return "redirect:/staff/orders/" + orderId;
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/staff/workspace/dashboard";
        }
    }
}