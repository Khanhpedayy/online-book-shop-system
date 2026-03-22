package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.repo.StaffPaymentQueryRepository;
import com.example.onlinebookshop.staff.service.StaffOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "status", required = false) String status,
                       Model model) {
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("rows", paymentRepo.listPayments(q, status, 200));
        return "staff/payment-logs";
    }

    @GetMapping("/{paymentId}")
    public String detail(@PathVariable long paymentId, Model model) {
        model.addAttribute("paymentId", paymentId);
        model.addAttribute("eventsJson", paymentRepo.getEventsJson(paymentId));
        model.addAttribute("orderId", paymentRepo.getOrderIdByPaymentId(paymentId));
        return "staff/payment-detail";
    }

    /**
     * Recheck: sync orders.payment_status dựa theo payments.status
     * - SUCCEEDED => PAID
     * - FAILED/EXPIRED/CANCELLED => FAILED
     * - REFUNDED => REFUNDED
     */
    @PostMapping("/{paymentId}/recheck")
    public String recheck(@PathVariable long paymentId, RedirectAttributes ra) {
        try {
            long orderId = paymentRepo.getOrderIdByPaymentId(paymentId);
            String pStatus = paymentRepo.getPaymentStatus(paymentId);
            if (pStatus == null) throw new RuntimeException("Payment status is null");

            String upper = pStatus.toUpperCase();
            if ("SUCCEEDED".equals(upper)) {
                // order.payment_status uses PAID
                staffOrderService.updatePaymentStatus(orderId, "PAID");
            } else if ("REFUNDED".equals(upper)) {
                staffOrderService.updatePaymentStatus(orderId, "REFUNDED");
            } else if ("FAILED".equals(upper) || "EXPIRED".equals(upper) || "CANCELLED".equals(upper)) {
                staffOrderService.updatePaymentStatus(orderId, "FAILED");
            } else {
                // CREATED/PENDING -> keep PENDING
                staffOrderService.updatePaymentStatus(orderId, "PENDING");
            }

            ra.addFlashAttribute("successMsg", "Đã recheck & sync payment_status cho order " + orderId);
            return "redirect:/staff/orders/" + orderId;
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/staff/payments/" + paymentId;
        }
    }
}