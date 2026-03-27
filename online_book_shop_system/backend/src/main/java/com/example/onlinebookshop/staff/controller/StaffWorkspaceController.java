package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.dto.OrderDetailView;
import com.example.onlinebookshop.staff.dto.OrderFilter;
import com.example.onlinebookshop.staff.service.StaffOrderService;
import com.example.onlinebookshop.staff.service.StaffPackingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/staff/workspace")
public class StaffWorkspaceController {

    private final StaffOrderService service;
    private final StaffPackingService packingService;

    public StaffWorkspaceController(StaffOrderService service,
                                    StaffPackingService packingService) {
        this.service = service;
        this.packingService = packingService;
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
                           HttpServletRequest request,
                           RedirectAttributes ra) {
        try {
            Map<Long, Integer> boxCounts = new LinkedHashMap<>();
            Map<Long, String> packingNotes = new LinkedHashMap<>();

            if (orderIds != null) {
                for (Long orderId : orderIds) {
                    String boxRaw = request.getParameter("boxCount_" + orderId);
                    String noteRaw = request.getParameter("packingNote_" + orderId);

                    int boxCount = 1;
                    if (boxRaw != null && !boxRaw.trim().isEmpty()) {
                        boxCount = Integer.parseInt(boxRaw.trim());
                    }

                    boxCounts.put(orderId, boxCount);
                    packingNotes.put(orderId, noteRaw);
                }
            }

            service.bulkPack(orderIds, boxCounts, packingNotes);
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

    @GetMapping("/packing-slip/bulk")
    public String bulkPackingSlip(@RequestParam("orderIds") List<Long> orderIds,
                                  Model model,
                                  RedirectAttributes ra) {
        try {
            model.addAttribute("views", packingService.getPackingViews(orderIds));
            return "staff/packing-slip-bulk";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/staff/workspace/packing";
        }
    }

    @GetMapping("/invoice/{orderId}")
    public String invoice(@PathVariable Long orderId,
                          Model model,
                          RedirectAttributes ra) {
        try {
            OrderDetailView v = service.getDetail(orderId);

            if (v == null) {
                ra.addFlashAttribute("errorMsg", "Không tìm thấy dữ liệu invoice cho đơn " + orderId);
                return "redirect:/staff/workspace/packing";
            }

            model.addAttribute("v", v);
            return "staff/invoice-print";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/staff/workspace/packing";
        }
    }

    @GetMapping("/invoice/bulk")
    public String bulkInvoice(@RequestParam("orderIds") List<Long> orderIds,
                              Model model,
                              RedirectAttributes ra) {
        try {
            List<OrderDetailView> views = new ArrayList<>();
            for (Long orderId : orderIds) {
                views.add(service.getDetail(orderId));
            }
            model.addAttribute("views", views);
            return "staff/invoice-print-bulk";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/staff/workspace/packing";
        }
    }

    private void prepareStage(OrderFilter filter, String stage, Model model) {
        filter.setStage(stage);
        model.addAttribute("stage", stage);
        model.addAttribute("filter", filter);
        model.addAttribute("orders", service.getAll(filter));
    }
}