package com.lautuquy.management.controller.customer;

import com.lautuquy.management.entity.*;
import com.lautuquy.management.repository.OrderItemRepository;
import com.lautuquy.management.service.BookingService;
import com.lautuquy.management.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller phục vụ khách hàng trực tiếp gọi món và theo dõi hóa đơn khi đang ngồi tại bàn (SEATED).
 */
@Controller
@RequestMapping("/customer/order")
public class CustomerOrderController {

    private final BookingService bookingService;
    private final OrderService orderService;
    private final OrderItemRepository orderItemRepository;

    public CustomerOrderController(BookingService bookingService,
                                   OrderService orderService,
                                   OrderItemRepository orderItemRepository) {
        this.bookingService = bookingService;
        this.orderService = orderService;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * Hiển thị trang Gọi món / Đơn hàng thời gian thực tại bàn của Khách hàng.
     */
    @GetMapping
    public String viewCustomerOrder(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/auth/login";
        }
        String username = authentication.getName();
        Booking seatedBooking = bookingService.getActiveSeatedBooking(username);

        if (seatedBooking == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn chưa có bàn ăn nào đang phục vụ.");
            return "redirect:/customer/menu";
        }

        Order activeOrder = orderService.getOrCreateActiveOrderForBooking(seatedBooking.getId());
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(activeOrder.getId());

        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("activeBooking", seatedBooking);
        model.addAttribute("activeOrder", activeOrder);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("seatedBooking", seatedBooking);
        model.addAttribute("isSeated", true);
        model.addAttribute("pageTitle", "Đơn Gọi Món Tại Bàn — " + (seatedBooking.getAssignedTable() != null ? "Bàn " + seatedBooking.getAssignedTable().getTableNumber() : "Bàn ăn"));

        return "customer/order";
    }

    /**
     * API AJAX xử lý Khách hàng bấm "Thêm món ăn" không bị giật/reload trang.
     */
    @PostMapping("/add-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addDishToOrderAjax(@RequestParam("dishId") Long dishId,
                                                                  @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                                                                  Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để thực hiện.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        String username = authentication.getName();
        Booking seatedBooking = bookingService.getActiveSeatedBooking(username);

        if (seatedBooking == null) {
            response.put("success", false);
            response.put("message", "Bạn chưa được nhận bàn tại nhà hàng để gọi món.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Order activeOrder = orderService.getOrCreateActiveOrderForBooking(seatedBooking.getId());
            OrderItem item = orderService.addItem(activeOrder.getId(), dishId, quantity);

            List<OrderItem> allItems = orderItemRepository.findByOrderId(activeOrder.getId());
            BigDecimal totalAmount = allItems.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            int totalItemCount = allItems.stream().mapToInt(OrderItem::getQuantity).sum();

            response.put("success", true);
            response.put("message", "Đã thêm món '" + item.getDish().getName() + "' vào đơn gọi bàn thành công!");
            response.put("dishName", item.getDish().getName());
            response.put("remainingQty", item.getDish().getQuantity());
            response.put("cartCount", totalItemCount);
            response.put("totalAmount", totalAmount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể thêm món: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API AJAX cập nhật số lượng món ăn trong đơn tại bàn (kiểm tra giới hạn tồn kho).
     */
    @PostMapping("/update-item-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateOrderItemAjax(@RequestParam("orderItemId") Long orderItemId,
                                                                   @RequestParam("quantity") int quantity,
                                                                   Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        try {
            OrderItem item = orderService.updateItem(orderItemId, quantity);
            Booking seatedBooking = bookingService.getActiveSeatedBooking(authentication.getName());
            Order activeOrder = orderService.getOrCreateActiveOrderForBooking(seatedBooking.getId());
            List<OrderItem> allItems = orderItemRepository.findByOrderId(activeOrder.getId());
            BigDecimal totalAmount = allItems.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);

            response.put("success", true);
            response.put("message", "Đã cập nhật số lượng!");
            response.put("subtotal", item != null ? item.getSubtotal() : BigDecimal.ZERO);
            response.put("quantity", item != null ? item.getQuantity() : 0);
            response.put("maxQty", item != null ? (item.getQuantity() + item.getDish().getQuantity()) : 0);
            response.put("totalAmount", totalAmount);
            response.put("removed", item == null);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            OrderItem item = orderItemRepository.findById(orderItemId).orElse(null);
            int maxAllowed = item != null ? (item.getQuantity() + (item.getDish() != null ? item.getDish().getQuantity() : 0)) : quantity;
            if (item != null) {
                try {
                    orderService.updateItem(orderItemId, maxAllowed);
                } catch (Exception ignored) {}
            }
            Booking seatedBooking = bookingService.getActiveSeatedBooking(authentication.getName());
            BigDecimal totalAmount = BigDecimal.ZERO;
            if (seatedBooking != null) {
                Order activeOrder = orderService.getOrCreateActiveOrderForBooking(seatedBooking.getId());
                List<OrderItem> allItems = orderItemRepository.findByOrderId(activeOrder.getId());
                totalAmount = allItems.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            }

            response.put("success", false);
            response.put("exceeded", true);
            response.put("maxAllowed", maxAllowed);
            response.put("subtotal", item != null ? item.getSubtotal() : BigDecimal.ZERO);
            response.put("totalAmount", totalAmount);
            response.put("message", "Vượt quá số lượng hiện có! Đã đưa số lượng chọn về tối đa " + maxAllowed + " suất.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể cập nhật: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API AJAX xóa món ăn khỏi đơn gọi bàn.
     */
    @PostMapping("/delete-item-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteOrderItemAjax(@RequestParam("orderItemId") Long orderItemId,
                                                                   Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        try {
            orderService.removeItem(orderItemId);
            Booking seatedBooking = bookingService.getActiveSeatedBooking(authentication.getName());
            BigDecimal totalAmount = BigDecimal.ZERO;
            if (seatedBooking != null) {
                Order activeOrder = orderService.getOrCreateActiveOrderForBooking(seatedBooking.getId());
                List<OrderItem> allItems = orderItemRepository.findByOrderId(activeOrder.getId());
                totalAmount = allItems.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            }

            response.put("success", true);
            response.put("message", "Đã xóa món ăn khỏi danh sách gọi món!");
            response.put("totalAmount", totalAmount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể xóa món: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Fallback POST "Thêm món ăn".
     */
    @PostMapping("/add")
    public String addDishToOrder(@RequestParam("dishId") Long dishId,
                                 @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/auth/login";
        }
        String username = authentication.getName();
        Booking seatedBooking = bookingService.getActiveSeatedBooking(username);

        if (seatedBooking == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn chưa được nhận bàn tại nhà hàng để gọi món.");
            return "redirect:/customer/menu";
        }

        try {
            Order activeOrder = orderService.getOrCreateActiveOrderForBooking(seatedBooking.getId());
            orderService.addItem(activeOrder.getId(), dishId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm món ăn vào đơn gọi bàn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm món: " + e.getMessage());
        }

        return "redirect:/customer/menu";
    }

    /**
     * Fallback POST cập nhật số lượng món ăn.
     */
    @PostMapping("/update-item")
    public String updateOrderItem(@RequestParam("orderItemId") Long orderItemId,
                                  @RequestParam("quantity") int quantity,
                                  RedirectAttributes redirectAttributes) {
        try {
            orderService.updateItem(orderItemId, quantity);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật: " + e.getMessage());
        }
        return "redirect:/customer/order";
    }

    /**
     * Fallback POST xóa món ăn.
     */
    @PostMapping("/delete-item")
    public String deleteOrderItem(@RequestParam("orderItemId") Long orderItemId,
                                  RedirectAttributes redirectAttributes) {
        try {
            orderService.removeItem(orderItemId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa món: " + e.getMessage());
        }
        return "redirect:/customer/order";
    }
}
