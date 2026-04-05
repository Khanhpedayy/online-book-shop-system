package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.Repository.UserRepository;
import com.example.onlinebookshop.staff.service.StaffDeliveryService;
import com.example.onlinebookshop.staff.service.StaffPackingService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff")
public class StaffDeliveryController {

    private final StaffPackingService packingService;
    private final StaffDeliveryService deliveryService;
    private final UserRepository userRepository;

    public StaffDeliveryController(StaffPackingService packingService,
                                   StaffDeliveryService deliveryService,
                                   UserRepository userRepository) {
        this.packingService = packingService;
        this.deliveryService = deliveryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/packing/orders/{orderId}")
    public String packDetail(@PathVariable long orderId, Model model) {
        model.addAttribute("view", packingService.getPackDetail(orderId));
        return "staff/pack-detail";
    }

    @PostMapping("/packing/orders/{orderId}/confirm")
    public String confirmPack(@PathVariable long orderId, RedirectAttributes ra) {
        try {
            packingService.confirmPacked(orderId, resolveActorUserId());
            ra.addFlashAttribute("successMsg", "Đã xác nhận đóng gói.");
            return "redirect:/staff/workspace/shipping";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/staff/packing/orders/" + orderId;
        }
    }

    @GetMapping("/orders/{orderId}/delivery-note")
    public String deliveryNote(@PathVariable long orderId, Model model) {
        model.addAttribute("view", deliveryService.getDeliveryDetail(orderId));
        return "staff/delivery-note";
    }

    @GetMapping("/delivery/orders/{orderId}")
    public String deliveryDetail(@PathVariable long orderId, Model model) {
        model.addAttribute("view", deliveryService.getDeliveryDetail(orderId));
        return "staff/delivery-detail";
    }

    @PostMapping("/delivery/orders/{orderId}/ship")
    public String startShip(@PathVariable long orderId, RedirectAttributes ra) {
        try {
            deliveryService.startShipping(orderId, resolveActorUserId());
            ra.addFlashAttribute("successMsg", "Đã bắt đầu giao hàng.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/delivery/orders/" + orderId;
    }

    @PostMapping("/delivery/orders/{orderId}/deliver")
    public String confirmDelivered(@PathVariable long orderId, RedirectAttributes ra) {
        try {
            deliveryService.confirmDeliveredSuccess(orderId, resolveActorUserId());
            ra.addFlashAttribute("successMsg", "Đã xác nhận giao thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/workspace/shipping";
    }

    @PostMapping("/delivery/orders/{orderId}/delivery-fail")
    public String confirmDeliveryFail(@PathVariable long orderId,
                                      @RequestParam(value = "cancelOrder", defaultValue = "false") boolean cancelOrder,
                                      @RequestParam(value = "markReturned", defaultValue = "false") boolean markReturned,
                                      @RequestParam(value = "note", required = false) String note,
                                      RedirectAttributes ra) {
        try {
            deliveryService.confirmDeliveryFail(orderId, cancelOrder, markReturned, note);
            ra.addFlashAttribute("successMsg", "Đã ghi nhận giao thất bại.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/workspace/shipping";
    }

    private Long resolveActorUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        return userRepository.findByEmailAndDeletedAtIsNull(authentication.getName())
                .map(user -> user.getId())
                .orElse(null);
    }
}