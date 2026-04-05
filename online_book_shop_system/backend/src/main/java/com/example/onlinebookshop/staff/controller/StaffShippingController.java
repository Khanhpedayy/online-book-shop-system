package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.Repository.UserRepository;
import com.example.onlinebookshop.staff.service.StaffShippingService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/orders")
public class StaffShippingController {

    private final StaffShippingService service;
    private final UserRepository userRepository;

    public StaffShippingController(StaffShippingService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    private Long getUserId(Authentication auth) {
        if (auth == null) return null;
        return userRepository.findByEmailAndDeletedAtIsNull(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

//    @GetMapping("/{orderId}/ship")
//    public String shipScreen(@PathVariable long orderId,
//                             Model model,
//                             RedirectAttributes ra) {
//        try {
//            model.addAttribute("v", service.getShippingView(orderId));
//            return "staff/ship";
//        } catch (Exception e) {
//            ra.addFlashAttribute("errorMsg", e.getMessage());
//            return "redirect:/staff/orders/" + orderId;
//        }
//    }

    @PostMapping("/{orderId}/ship/confirm")
    public String confirmShip(@PathVariable long orderId,
                              @RequestParam(value = "carrier", required = false) String carrier,
                              @RequestParam(value = "trackingCode", required = false) String trackingCode,
                              Authentication auth,
                              RedirectAttributes ra) {
        try {
            if (carrier == null || carrier.trim().isEmpty()) {
                Long staffId = (auth != null) ? getUserId(auth) : null;
                carrier = (staffId != null) ? String.valueOf(staffId) : "Nhân viên giao hàng";
            }
            service.confirmShipped(orderId, carrier, trackingCode);
            ra.addFlashAttribute("successMsg", "Đã xác nhận SHIPPED thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/workspace/shipping";
    }

    @PostMapping("/{orderId}/ship")
    public String shipOrder(@PathVariable long orderId,
                            Authentication auth,
                            RedirectAttributes ra) {
        try {
            Long staffId = (auth != null) ? getUserId(auth) : null;
            String shippedBy = (staffId != null) ? String.valueOf(staffId) : "Nhân viên giao hàng";
            service.confirmShipped(orderId, shippedBy, null);
            ra.addFlashAttribute("successMsg", "Đã giao đơn thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/staff/workspace/shipping"; // quay lại list
    }
}