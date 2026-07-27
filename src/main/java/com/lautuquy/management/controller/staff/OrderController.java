package com.lautuquy.management.controller.staff;

import com.lautuquy.management.entity.Booking;
import com.lautuquy.management.entity.Dish;
import com.lautuquy.management.entity.Order;
import com.lautuquy.management.service.BookingService;
import com.lautuquy.management.service.DishService;
import com.lautuquy.management.service.OrderService;
import com.lautuquy.management.service.TableService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/staff")
public class OrderController {

    private final OrderService orderService;
    private final BookingService bookingService;
    private final DishService dishService;
    private final TableService tableService;

    public OrderController(OrderService orderService,
                           BookingService bookingService,
                           DishService dishService,
                           TableService tableService) {
        this.orderService = orderService;
        this.bookingService = bookingService;
        this.dishService = dishService;
        this.tableService = tableService;
    }

    /**
     * View Bảng trạng thái bàn (Table Board) dùng AJAX polling
     */
    @GetMapping("/table-board")
    public String tableBoard(Model model) {
        model.addAttribute("tables", tableService.getAllTables());
        model.addAttribute("pageTitle", "Sơ Đồ Bàn Ăn — Staff");
        return "staff/table-board";
    }

    /**
     * View Chi tiết đơn gọi món tại bàn của một Booking
     */
    @GetMapping("/orders/booking/{bookingId}")
    public String orderDetail(@PathVariable Long bookingId, Model model) {
        Booking booking = bookingService.getBookingById(bookingId);
        Order order = orderService.getOrCreateActiveOrderForBooking(bookingId);

        List<Dish> availableDishes = dishService.getAllDishes();

        // Tính tổng tiền hiện tại của Order từ actual_price * quantity
        BigDecimal totalAmount = order.getOrderItems().stream()
                .map(item -> item.getActualPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("booking", booking);
        model.addAttribute("order", order);
        model.addAttribute("availableDishes", availableDishes);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("pageTitle", "Gọi Món Tại Bàn #" + (booking.getAssignedTable() != null ? booking.getAssignedTable().getTableNumber() : "Chưa gán"));

        return "staff/order-detail";
    }

    /**
     * Thêm món vào Order
     */
    @PostMapping("/orders/{orderId}/items/add")
    public String addItem(@PathVariable Long orderId,
                          @RequestParam("bookingId") Long bookingId,
                          @RequestParam("dishId") Long dishId,
                          @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                          RedirectAttributes redirectAttributes) {
        try {
            orderService.addItem(orderId, dishId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm món vào đơn gọi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm món: " + e.getMessage());
        }
        return "redirect:/staff/orders/booking/" + bookingId;
    }

    /**
     * Cập nhật số lượng món trong Order
     */
    @PostMapping("/orders/items/{itemId}/update")
    public String updateItem(@PathVariable Long itemId,
                             @RequestParam("bookingId") Long bookingId,
                             @RequestParam("quantity") int quantity,
                             RedirectAttributes redirectAttributes) {
        try {
            orderService.updateItem(itemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật số lượng món.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật: " + e.getMessage());
        }
        return "redirect:/staff/orders/booking/" + bookingId;
    }

    /**
     * Xóa món khỏi Order
     */
    @PostMapping("/orders/items/{itemId}/remove")
    public String removeItem(@PathVariable Long itemId,
                             @RequestParam("bookingId") Long bookingId,
                             RedirectAttributes redirectAttributes) {
        try {
            orderService.removeItem(itemId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa món khỏi đơn gọi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa món: " + e.getMessage());
        }
        return "redirect:/staff/orders/booking/" + bookingId;
    }
}
