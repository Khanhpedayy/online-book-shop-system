package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.dto.OrderFilter;
import com.example.onlinebookshop.staff.service.StaffOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequestMapping("/staff/workspace")
public class StaffWorkspaceController {

    private final StaffOrderService service;

    public StaffWorkspaceController(StaffOrderService service) {
        this.service = service;
    }

    @GetMapping({"", "/"})
    public String root() {
        return "redirect:/staff/workspace/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stage", "dashboard");
        model.addAttribute("stats", service.getDashboardStats());
        model.addAttribute("todoOrders", service.getTodoList());
        return "staff/workspace-dashboard";
    }

    @GetMapping("/confirmed")
    public String confirmed(@ModelAttribute("filter") OrderFilter filter, Model model) {
        prepareStage(filter, "confirmed", model);
        return "staff/workspace-confirmed";
    }

    @GetMapping("/allocate")
    public String allocate(@ModelAttribute("filter") OrderFilter filter, Model model) {
        prepareStage(filter, "allocate", model);
        return "staff/workspace-allocate";
    }

    @GetMapping("/packing")
    public String packing(@ModelAttribute("filter") OrderFilter filter, Model model) {
        prepareStage(filter, "packing", model);
        return "staff/workspace-packing";
    }

    @GetMapping("/shipping")
    public String shipping(@ModelAttribute("filter") OrderFilter filter, Model model) {
        prepareStage(filter, "shipping", model);
        return "staff/workspace-shipping";
    }

    @GetMapping("/delivery-return")
    public String deliveryReturn(@ModelAttribute("filter") OrderFilter filter, Model model) {
        prepareStage(filter, "delivery-return", model);
        return "staff/workspace-delivery-return";
    }

    @PostMapping("/confirmed/bulk-confirm")
    public String bulkConfirm(@RequestParam(name = "orderIds", required = false) List<Long> orderIds,
                              RedirectAttributes ra) {
        try {
            service.bulkConfirm(orderIds);
            ra.addFlashAttribute("successMsg", "Đã chuyển các đơn đã chọn sang CONFIRMED.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/workspace/confirmed";
    }

    @PostMapping("/packing/bulk-pack")
    public String bulkPack(@RequestParam(name = "orderIds", required = false) List<Long> orderIds,
                           RedirectAttributes ra) {
        try {
            service.bulkPack(orderIds);
            ra.addFlashAttribute("successMsg", "Đã chuyển các đơn đã chọn sang PACKED.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/workspace/packing";
    }

    @PostMapping("/shipping/bulk-ship")
    public String bulkShip(@RequestParam(name = "orderIds", required = false) List<Long> orderIds,
                           @RequestParam(name = "carrier", required = false) String carrier,
                           RedirectAttributes ra) {
        try {
            service.bulkShip(orderIds, carrier);
            ra.addFlashAttribute("successMsg", "Đã chuyển các đơn đã chọn sang SHIPPED.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/workspace/shipping";
    }

    @PostMapping("/delivery-return/bulk-deliver")
    public String bulkDeliver(@RequestParam(name = "orderIds", required = false) List<Long> orderIds,
                              RedirectAttributes ra) {
        try {
            service.bulkDeliver(orderIds);
            ra.addFlashAttribute("successMsg", "Đã đánh dấu các đơn đã chọn là DELIVERED.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/workspace/delivery-return";
    }

    @PostMapping("/allocate/{orderId}/confirm-auto-pick")
    public String confirmAutoPick(@PathVariable Long orderId,
                                  RedirectAttributes ra) {
        try {
            service.confirmAutoAllocateAndPick(orderId);
            ra.addFlashAttribute("successMsg", "Đã auto allocate + pick xong cho đơn " + orderId);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/workspace/allocate";
    }

    @PostMapping("/delivery-return/create-return-intake")
    public String createReturnIntake(@RequestParam("orderId") Long orderId,
                                     @RequestParam("copyCodes") String copyCodes,
                                     @RequestParam(value = "reason", required = false) String reason,
                                     @RequestParam(value = "receivedConditionGrade", required = false) String receivedConditionGrade,
                                     @RequestParam(value = "receivedConditionNote", required = false) String receivedConditionNote,
                                     RedirectAttributes ra) {
        try {
            service.createReturnIntakeMulti(orderId, copyCodes, reason, receivedConditionGrade, receivedConditionNote);
            ra.addFlashAttribute("successMsg", "Đã tạo return intake cho đơn " + orderId);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/workspace/delivery-return";
    }

    private void prepareStage(OrderFilter filter, String stage, Model model) {
        filter.setStage(stage);
        model.addAttribute("stage", stage);
        model.addAttribute("filter", filter);
        model.addAttribute("orders", service.getAll(filter));
    }
}