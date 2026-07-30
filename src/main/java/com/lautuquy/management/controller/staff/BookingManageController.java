package com.lautuquy.management.controller.staff;

import com.lautuquy.management.entity.Booking;
import com.lautuquy.management.entity.Invoice;
import com.lautuquy.management.entity.RestaurantTable;
import com.lautuquy.management.service.BookingService;
import com.lautuquy.management.service.InvoiceService;
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
    private final InvoiceService invoiceService;

    public BookingManageController(BookingService bookingService,
                                  TableService tableService,
                                  InvoiceService invoiceService) {
        this.bookingService = bookingService;
        this.tableService = tableService;
        this.invoiceService = invoiceService;
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

        // Lấy danh sách hóa đơn tương ứng với các đơn đặt bàn (để kiểm tra xem khách đã thanh toán chưa)
        java.util.Map<Long, Invoice> invoiceMap = new java.util.HashMap<>();
        java.util.Map<Long, com.lautuquy.management.dto.response.BookingDetailDto> bookingDetailsMap = new java.util.HashMap<>();
        for (Booking b : filteredBookings) {
            Invoice inv = invoiceService.getInvoiceByBookingId(b.getId());
            if (inv != null) {
                invoiceMap.put(b.getId(), inv);
            }
            bookingDetailsMap.put(b.getId(), bookingService.getBookingDetail(b.getId()));
        }

        model.addAttribute("bookings", filteredBookings);
        model.addAttribute("invoiceMap", invoiceMap);
        model.addAttribute("bookingDetailsMap", bookingDetailsMap);
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

    @PostMapping("/{id}/receive")
    public String receiveBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.receiveBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã nhận bàn thành công cho đơn #" + id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể nhận bàn: " + e.getMessage());
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

    @PostMapping("/{id}/confirm-payment")
    public String confirmPayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Invoice invoice = invoiceService.confirmPaymentByStaff(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xác nhận thanh toán thành công cho đơn đặt bàn #" + id + "! Hóa đơn #" + invoice.getId() + " đã hoàn tất.");
            return "redirect:/staff/invoices/" + invoice.getId() + "/print";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xác nhận thanh toán: " + e.getMessage());
            return "redirect:/staff/bookings";
        }
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
