package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.service.StaffPackingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/orders")
public class StaffPackingController {

    private final StaffPackingService service;

    public StaffPackingController(StaffPackingService service) {
        this.service = service;
    }

    @GetMapping("/{orderId}/pack")
    public String packScreen(@PathVariable long orderId,
                             Model model,
                             RedirectAttributes ra) {
        try {
            model.addAttribute("v", service.getPackingView(orderId));
            return "staff/pack";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/staff/orders/" + orderId;
        }
    }

    @PostMapping("/{orderId}/pack/confirm")
    public String confirmPack(@PathVariable long orderId,
                              @RequestParam("boxCount") int boxCount,
                              @RequestParam(value = "packingNote", required = false) String packingNote,
                              RedirectAttributes ra) {
        try {
            service.confirmPacked(orderId, boxCount, packingNote);
            ra.addFlashAttribute("successMsg", "Đã xác nhận PACKED thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/orders/" + orderId + "/pack";
    }

    @GetMapping("/{orderId}/packing-slip")
    public String packingSlip(@PathVariable long orderId,
                              Model model,
                              RedirectAttributes ra) {
        try {
            model.addAttribute("v", service.getPackingView(orderId));
            return "staff/packing-slip";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/staff/orders/" + orderId;
        }
    }
}