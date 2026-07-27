package com.lautuquy.management.controller.staff;

import com.lautuquy.management.entity.Booking;
import com.lautuquy.management.entity.RestaurantTable;
import com.lautuquy.management.service.BookingService;
import com.lautuquy.management.service.TableService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/staff/bookings")
public class BookingManageController {

    private final BookingService bookingService;
    private final TableService tableService;

    public BookingManageController(BookingService bookingService, TableService tableService) {
        this.bookingService = bookingService;
        this.tableService = tableService;
    }

    @GetMapping
    public String listBookings(@RequestParam(value = "status", required = false) String statusFilter, Model model) {
        List<Booking> allBookings = bookingService.getAllBookings();

        List<Booking> filteredBookings;
        if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
            filteredBookings = allBookings.stream()
                    .filter(b -> b.getStatus().name().equalsIgnoreCase(statusFilter))
                    .toList();
        } else {
            filteredBookings = allBookings;
        }

        // Lấy danh sách bàn đang EMPTY để dùng cho Modal xếp bàn
        List<RestaurantTable> emptyTables = tableService.getAllTables().stream()
                .filter(t -> t.getStatus() == RestaurantTable.Status.EMPTY || t.getStatus() == RestaurantTable.Status.RESERVED)
                .toList();

        model.addAttribute("bookings", filteredBookings);
        model.addAttribute("selectedStatus", statusFilter != null ? statusFilter : "ALL");
        model.addAttribute("emptyTables", emptyTables);
        model.addAttribute("pageTitle", "Quản Lý Đặt Bàn — Staff");

        return "staff/booking-list";
    }

    @PostMapping("/{id}/confirm")
    public String confirmBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.confirmBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận đơn đặt bàn #" + id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xác nhận: " + e.getMessage());
        }
        return "redirect:/staff/bookings";
    }

    @PostMapping("/{id}/seat")
    public String seatBooking(@PathVariable Long id,
                              @RequestParam("tableId") Long tableId,
                              RedirectAttributes redirectAttributes) {
        try {
            bookingService.seatBooking(id, tableId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xếp bàn thành công cho đơn #" + id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xếp bàn: " + e.getMessage());
        }
        return "redirect:/staff/bookings";
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBookingByStaff(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn đặt bàn #" + id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn: " + e.getMessage());
        }
        return "redirect:/staff/bookings";
    }
}
