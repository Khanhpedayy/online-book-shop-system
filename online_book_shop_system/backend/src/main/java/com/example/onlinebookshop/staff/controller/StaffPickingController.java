package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.service.StaffPickingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/orders")
public class StaffPickingController {

    private final StaffPickingService picking;

    public StaffPickingController(StaffPickingService picking) {
        this.picking = picking;
    }

    @PostMapping("/{orderId}/split-item")
    public String splitItem(@PathVariable long orderId,
                            @RequestParam("orderItemId") long orderItemId,
                            RedirectAttributes ra) {
        try {
            int affected = picking.splitOrderItemToSingleUnits(orderId, orderItemId);
            ra.addFlashAttribute("successMsg", "Đã tách dòng thành " + affected + " dòng quantity = 1.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/orders/" + orderId;
    }

    @PostMapping("/{orderId}/allocate-auto")
    public String autoAllocate(@PathVariable long orderId, RedirectAttributes ra) {
        try {
            var result = picking.autoAllocate(orderId);

            if (result.warnings().isEmpty()) {
                ra.addFlashAttribute("successMsg",
                        "Auto allocate thành công. Số item đã allocate: " + result.allocatedCount());
            } else {
                ra.addFlashAttribute("successMsg",
                        "Auto allocate xong. Số item đã allocate: " + result.allocatedCount()
                                + ". Cảnh báo: " + String.join(" | ", result.warnings()));
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/staff/orders/" + orderId;
    }

    @PostMapping("/{orderId}/pick-choose")
    public String pickChoose(@PathVariable long orderId,
                             @RequestParam("orderItemId") long orderItemId,
                             @RequestParam("copyId") long copyId,
                             RedirectAttributes ra) {
        try {
            picking.chooseAvailableCopyAndPick(orderId, orderItemId, copyId);
            ra.addFlashAttribute("successMsg", "Đã chọn copy khả dụng và pick thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/orders/" + orderId;
    }

    @PostMapping("/{orderId}/pick-create-from-lot")
    public String pickCreateFromLot(@PathVariable long orderId,
                                    @RequestParam("orderItemId") long orderItemId,
                                    @RequestParam("lotId") long lotId,
                                    @RequestParam("copyCode") String copyCode,
                                    RedirectAttributes ra) {
        try {
            picking.createCopyFromLotAndPick(orderId, orderItemId, lotId, copyCode);
            ra.addFlashAttribute("successMsg", "Đã tạo copy từ lot, bind và pick thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/orders/" + orderId;
    }

    @PostMapping("/{orderId}/pick-confirm")
    public String pickConfirm(@PathVariable long orderId,
                              @RequestParam("orderItemId") long orderItemId,
                              RedirectAttributes ra) {
        try {
            picking.confirmAllocatedPick(orderId, orderItemId);
            ra.addFlashAttribute("successMsg", "Đã xác nhận lấy cuốn đã allocate.");
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
            ra.addFlashAttribute("successMsg", "Đã bỏ pick cho dòng hàng.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/orders/" + orderId;
    }
}