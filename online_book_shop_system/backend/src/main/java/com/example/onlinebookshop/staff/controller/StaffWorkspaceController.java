package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.dto.OrderFilter;
import com.example.onlinebookshop.staff.service.StaffDashboardStats;
import com.example.onlinebookshop.staff.service.StaffOrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.LinkedHashMap;
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

            var todoOrders = orderService.getTodoList();
            if (todoOrders != null) {
                model.addAttribute("todoOrders", todoOrders);
            }
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
            Model model,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : "STAFF";

        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("q", q);

        model.addAttribute("role", "STAFF");
        model.addAttribute("username", username);
        model.addAttribute("filter", filter);
        model.addAttribute("dbError", null);

        try {
            OrderFilter orderFilter = new OrderFilter();
            orderFilter.setQ(q);              // nếu class bạn có field này
            orderFilter.setStage("allocate"); // 🔥 quan trọng

            model.addAttribute("orders", orderService.getAll(orderFilter));

        } catch (Exception ex) {
            model.addAttribute("orders", Collections.emptyList());
            model.addAttribute("dbError", ex.getMessage());
        }

        return "staff/workspace-allocate";
    }

    @GetMapping("/staff/workspace/delivery-return")
    public String deliveryReturn(Model model, Authentication authentication) {

        String username = authentication != null ? authentication.getName() : "STAFF";

        model.addAttribute("role", "STAFF");
        model.addAttribute("username", username);

        try {
            OrderFilter filter = new OrderFilter();
            filter.setStage("delivery-return"); // 🔥 quan trọng

            model.addAttribute("orders", orderService.getAll(filter));

        } catch (Exception e) {
            model.addAttribute("orders", Collections.emptyList());
            model.addAttribute("dbError", e.getMessage());
        }

        return "staff/workspace-delivery-return"; // 👈 đúng tên file mày gửi
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
}