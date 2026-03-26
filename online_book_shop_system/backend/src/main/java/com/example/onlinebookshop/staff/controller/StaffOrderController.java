package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.dto.OrderDetailView;
import com.example.onlinebookshop.staff.service.StaffOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/orders")
public class StaffOrderController {

    private final StaffOrderService service;

    public StaffOrderController(StaffOrderService service) {
        this.service = service;
    }

    @GetMapping
    public String listRedirect() {
        return "redirect:/staff/workspace/dashboard";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        OrderDetailView order = service.getDetail(id);
        model.addAttribute("order", order);
        return "staff/order-detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam("newStatus") String newStatus,
                               RedirectAttributes ra) {
        try {
            service.updateStatus(id, newStatus);
            ra.addFlashAttribute("successMsg", "Đã cập nhật trạng thái đơn hàng.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/orders/" + id;
    }

    @PostMapping("/{id}/note")
    public String updateNote(@PathVariable Long id,
                             @RequestParam("staffNote") String staffNote,
                             RedirectAttributes ra) {
        try {
            service.updateStaffNote(id, staffNote);
            ra.addFlashAttribute("successMsg", "Đã lưu ghi chú nội bộ.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/orders/" + id;
    }

    @PostMapping("/{id}/shipment")
    public String updateShipment(@PathVariable Long id,
                                 @RequestParam(value = "carrier", required = false) String carrier,
                                 @RequestParam(value = "trackingCode", required = false) String trackingCode,
                                 RedirectAttributes ra) {
        try {
            service.updateShipment(id, carrier, trackingCode);
            ra.addFlashAttribute("successMsg", "Đã lưu thông tin shipment.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/orders/" + id;
    }
}