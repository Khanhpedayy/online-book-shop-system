package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.dto.OrderFilter;
import com.example.onlinebookshop.staff.dto.OrderListRow;
import com.example.onlinebookshop.staff.service.StaffDashboardStats;
import com.example.onlinebookshop.staff.service.StaffOrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class StaffWorkspaceController {


    private final StaffOrderService orderService;

    public StaffWorkspaceController(StaffOrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/staff/workspace/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "STAFF";

        model.addAttribute("role", "STAFF");
        model.addAttribute("username", username);
        model.addAttribute("stats", new StaffDashboardStats(0, 0, 0, 0, 0));
        model.addAttribute("todoOrders", Collections.emptyList());
        model.addAttribute("dbError", null);

        try {
            StaffDashboardStats stats = orderService.getDashboardStats();
            if (stats != null) {
                model.addAttribute("stats", stats);
            }

            // 🔥 FIX Ở ĐÂY
            OrderFilter filter = new OrderFilter();
            filter.setStage("confirmed"); // chỉ lấy CONFIRMED

            List<OrderListRow> todoOrders = orderService.getAll(filter);

            model.addAttribute("todoOrders", todoOrders);

        } catch (Exception ex) {
            model.addAttribute("dbError", ex.getMessage());
        }

        return "staff/workspace-dashboard";
    }

    @GetMapping("/staff/workspace/confirmed")
    public String confirmed() {
        return "redirect:/staff/workspace/allocate";
    }

    @GetMapping("/staff/workspace/allocate")
    public String allocate(
            @RequestParam(name = "q", required = false, defaultValue = "") String q,
            @RequestParam(name = "status", required = false, defaultValue = "") String status,
            Model model,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : "STAFF";

        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("q", q);
        filter.put("status", status);

        model.addAttribute("role", "STAFF");
        model.addAttribute("username", username);
        model.addAttribute("filter", filter);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("dbError", null);

        try {
            OrderFilter orderFilter = new OrderFilter();
            orderFilter.setQ(q);
            orderFilter.setStage("allocate");
            if (status != null && !status.isEmpty()) {
                orderFilter.setStatus(status);
            } else {
                orderFilter.setStatus("CONFIRMED");
            }
            model.addAttribute("orders", orderService.getAll(orderFilter));

        } catch (Exception ex) {
            model.addAttribute("orders", Collections.emptyList());
            model.addAttribute("dbError", ex.getMessage());
        }

        return "staff/workspace-allocate";
    }

    @GetMapping("/staff/workspace/delivery-return")
    public String deliveryReturn(
            @RequestParam(name = "q", required = false, defaultValue = "") String q,
            @RequestParam(name = "status", required = false, defaultValue = "") String status,
            Model model, Authentication authentication) {

        String username = authentication != null ? authentication.getName() : "STAFF";

        model.addAttribute("role", "STAFF");
        model.addAttribute("username", username);
        model.addAttribute("q", q);
        model.addAttribute("status", status);

        try {
            OrderFilter filter = new OrderFilter();
            filter.setStage("delivery-return");
            if (q != null && !q.trim().isEmpty()) {
                filter.setQ(q.trim());
            }
            if (status != null && !status.trim().isEmpty()) {
                filter.setStatus(status.trim());
            }

            model.addAttribute("orders", orderService.getAll(filter));

        } catch (Exception e) {
            model.addAttribute("orders", Collections.emptyList());
            model.addAttribute("dbError", e.getMessage());
        }

        return "staff/workspace-delivery-return";
    }
    @PostMapping("/staff/workspace/delivery-return/create-return-intake")
    public String createReturnIntake(@RequestParam("orderId") Long orderId,
                                     @RequestParam("copyCodes") String copyCodes,
                                     @RequestParam(value = "reason", required = false) String reason,
                                     @RequestParam(value = "receivedConditionGrade", required = false) String receivedConditionGrade,
                                     @RequestParam(value = "receivedConditionNote", required = false) String receivedConditionNote,
                                     RedirectAttributes ra) {
        try {
            orderService.createReturnIntakeMulti(orderId, copyCodes, reason, receivedConditionGrade, receivedConditionNote);
            ra.addFlashAttribute("successMessage", "Đã tạo return intake cho đơn " + orderId);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/workspace/delivery-return";
    }

    @PostMapping("/staff/workspace/delivery-return/{id}/success")
    public String deliverSuccess(@PathVariable Long id, RedirectAttributes ra) {
        try {
            orderService.updateStatus(id, "COMPLETED");
            ra.addFlashAttribute("successMessage", "Đã đánh dấu giao hàng thành công đơn " + id);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/workspace/delivery-return";
    }

    @PostMapping("/staff/workspace/delivery-return/{id}/fail")
    public String deliverFail(@PathVariable Long id, @RequestParam("reason") String reason, RedirectAttributes ra) {
        try {
            orderService.markDeliveryFailed(id, reason);
            ra.addFlashAttribute("successMessage", "Giao hàng thất bại đơn " + id);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/workspace/delivery-return";
    }
}