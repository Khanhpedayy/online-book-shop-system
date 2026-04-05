package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.Repository.UserRepository;
import com.example.onlinebookshop.staff.dto.OrderDetailView;
import com.example.onlinebookshop.staff.service.StaffOrderService;
import com.example.onlinebookshop.staff.service.StaffPickingService;
import com.example.onlinebookshop.staff.service.StockOutService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/orders")
public class StaffOrderController {

    private final StaffOrderService service;
    private final StockOutService stockOutService;
    private final StaffPickingService staffPickingService;
    private final UserRepository userRepository;

    public StaffOrderController(StaffOrderService service,
                                StockOutService stockOutService,
                                StaffPickingService staffPickingService,
                                UserRepository userRepository) {
        this.service = service;
        this.stockOutService = stockOutService;
        this.staffPickingService = staffPickingService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listRedirect() {
        return "redirect:/staff/workspace/dashboard";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        OrderDetailView order = service.getDetail(id);
        var pickUi = staffPickingService.buildPickUi(id);

        model.addAttribute("order", order);
        model.addAttribute("activeStockOut", stockOutService.getActiveStockOut(id).orElse(null));
        model.addAttribute("availableCopiesByItemId", pickUi.availableCopiesByItemId());
        model.addAttribute("availableLotsByItemId", pickUi.availableLotsByItemId());
        model.addAttribute("boundCopiesByItemId", pickUi.boundCopiesByItemId());

        return "staff/order-detail";
    }

    @PostMapping("/{id}/create-stock-out")
    public String createStockOut(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Long stockOutId = stockOutService.createStockOut(id, resolveActorUserId());
            ra.addFlashAttribute("successMsg", "Đã tạo phiếu xuất kho.");
            return "redirect:/staff/stock-outs/" + stockOutId + "/pick";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/staff/orders/" + id;
        }
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