package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.Repository.UserRepository;
import com.example.onlinebookshop.staff.service.StockOutService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/stock-outs")
public class StaffStockOutController {

    private final StockOutService stockOutService;
    private final UserRepository userRepository;

    public StaffStockOutController(StockOutService stockOutService,
                                   UserRepository userRepository) {
        this.stockOutService = stockOutService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{stockOutId}/pick")
    public String pickPage(@PathVariable Long stockOutId, Model model) {
        StockOutService.PickPageView page = stockOutService.getPickPage(stockOutId);

        model.addAttribute("stockOut", page.getStockOut());
        model.addAttribute("items", page.getItems());
        model.addAttribute("totalItems", page.getTotalItems());
        model.addAttribute("pickedItems", page.getPickedItems());
        model.addAttribute("missingItems", page.getMissingItems());

        return "staff/stock-out-pick";
    }

    @PostMapping("/{stockOutId}/pick/{itemId}")
    public String pickItem(@PathVariable Long stockOutId,
                           @PathVariable Long itemId,
                           RedirectAttributes ra) {
        try {
            stockOutService.pickItem(stockOutId, itemId, resolveActorUserId());
            ra.addFlashAttribute("successMsg", "Đã pick item thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/stock-outs/" + stockOutId + "/pick";
    }

    @PostMapping("/{stockOutId}/missing/{itemId}")
    public String reportMissing(@PathVariable Long stockOutId,
                                @PathVariable Long itemId,
                                @RequestParam(value = "note", required = false) String note,
                                RedirectAttributes ra) {
        try {
            stockOutService.reportMissing(stockOutId, itemId, note);
            ra.addFlashAttribute("successMsg", "Đã báo thiếu hàng cho item.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/stock-outs/" + stockOutId + "/pick";
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