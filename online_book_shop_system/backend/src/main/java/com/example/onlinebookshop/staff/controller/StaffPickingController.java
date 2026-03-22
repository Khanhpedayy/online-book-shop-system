package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.service.StaffPickingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller cho Allocation/Picking ngay trong màn Order Detail.
 */
@Controller
@RequestMapping("/staff/orders")
public class StaffPickingController {

    private final StaffPickingService picking;

    public StaffPickingController(StaffPickingService picking) {
        this.picking = picking;
    }

    @PostMapping("/{orderId}/allocate-auto")
    public String autoAllocate(@PathVariable long orderId, RedirectAttributes ra) {
        try {
            var rs = picking.autoAllocate(orderId);
            if (!rs.warnings().isEmpty()) {
                ra.addFlashAttribute("warnMsg", "Allocated=" + rs.allocatedCount() + ". Warnings: " + String.join(" | ", rs.warnings()));
            } else {
                ra.addFlashAttribute("successMsg", "Auto allocate thành công. Allocated=" + rs.allocatedCount());
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/orders/" + orderId;
    }

    @PostMapping("/{orderId}/pick-scan")
    public String pickScan(@PathVariable long orderId,
                           @RequestParam("orderItemId") long orderItemId,
                           @RequestParam("copyCode") String copyCode,
                           RedirectAttributes ra) {
        try {
            picking.pickByScan(orderId, orderItemId, copyCode);
            ra.addFlashAttribute("successMsg", "Đã pick theo scan copyCode=" + copyCode);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/orders/" + orderId;
    }

    @PostMapping("/{orderId}/unpick")
    public String unpick(@PathVariable long orderId,
                         @RequestParam("orderItemId") long orderItemId,
                         RedirectAttributes ra) {
        try {
            picking.unpick(orderItemId);
            ra.addFlashAttribute("successMsg", "Đã unpick orderItemId=" + orderItemId);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/orders/" + orderId;
    }
}