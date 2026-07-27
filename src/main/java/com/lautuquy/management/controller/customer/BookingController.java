package com.lautuquy.management.controller.customer;

import com.lautuquy.management.dto.request.BookingRequest;
import com.lautuquy.management.entity.Booking;
import com.lautuquy.management.entity.Dish;
import com.lautuquy.management.service.AccountService;
import com.lautuquy.management.service.BookingService;
import com.lautuquy.management.service.DishService;
import com.lautuquy.management.service.TableService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/customer/booking")
public class BookingController {

    private final BookingService bookingService;
    private final TableService tableService;
    private final DishService dishService;
    private final AccountService accountService;

    public BookingController(BookingService bookingService,
                             TableService tableService,
                             DishService dishService,
                             AccountService accountService) {
        this.bookingService = bookingService;
        this.tableService = tableService;
        this.dishService = dishService;
        this.accountService = accountService;
    }

    @GetMapping
    public String showBookingForm(Model model, Principal principal) {
        BookingRequest bookingRequest = new BookingRequest();
        Booking seatedBooking = null;
        if (principal != null) {
            var account = accountService.loadUserByUsername(principal.getName());
            if (account instanceof com.lautuquy.management.entity.Account acc) {
                bookingRequest.setCustomerName(acc.getFullName());
                bookingRequest.setCustomerPhone(acc.getPhone());
            }
            seatedBooking = bookingService.getActiveSeatedBooking(principal.getName());
        }

        model.addAttribute("bookingRequest", bookingRequest);
        model.addAttribute("tableTypes", tableService.getAllTableTypes());
        List<Dish> availableDishes = dishService.getAllDishes();
        model.addAttribute("availableDishes", availableDishes);
        model.addAttribute("seatedBooking", seatedBooking);
        model.addAttribute("isSeated", seatedBooking != null);
        model.addAttribute("pageTitle", "Đặt Bàn Trước");

        return "customer/booking-form";
    }

    @PostMapping
    public String processBooking(@Valid @ModelAttribute("bookingRequest") BookingRequest request,
                                 BindingResult bindingResult,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        Booking seatedBooking = null;
        if (principal != null) {
            seatedBooking = bookingService.getActiveSeatedBooking(principal.getName());
        }

        if (seatedBooking != null) {
            model.addAttribute("errorMessage", "Bạn đang ngồi tại bàn ăn (" + (seatedBooking.getAssignedTable() != null ? "Bàn " + seatedBooking.getAssignedTable().getTableNumber() : "Bàn ăn") + "). Không thể tạo thêm đơn đặt bàn mới khi chưa hoàn tất!");
            model.addAttribute("seatedBooking", seatedBooking);
            model.addAttribute("isSeated", true);
            model.addAttribute("tableTypes", tableService.getAllTableTypes());
            model.addAttribute("availableDishes", dishService.getAllDishes());
            return "customer/booking-form";
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (request.getBookingDate() != null && request.getBookingDate().isBefore(today)) {
            bindingResult.rejectValue("bookingDate", "error.bookingDate", "Ngày đặt bàn không thể ở trong quá khứ.");
        }
        if (request.getBookingDate() != null && request.getBookingDate().isEqual(today)) {
            if (request.getBookingTime() != null && request.getBookingTime().isBefore(now)) {
                bindingResult.rejectValue("bookingTime", "error.bookingTime", "Giờ đặt bàn không thể trước thời gian hiện tại đối với ngày hôm nay.");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("tableTypes", tableService.getAllTableTypes());
            List<Dish> availableDishes = dishService.getAllDishes();
            model.addAttribute("availableDishes", availableDishes);
            return "customer/booking-form";
        }

        try {
            bookingService.createBooking(principal.getName(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt bàn thành công! Đơn của bạn đang chờ nhà hàng tiếp nhận.");
            return "redirect:/customer/booking/history";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Không thể tạo đơn đặt bàn: " + e.getMessage());
            model.addAttribute("tableTypes", tableService.getAllTableTypes());
            List<Dish> availableDishes = dishService.getAllDishes();
            model.addAttribute("availableDishes", availableDishes);
            return "customer/booking-form";
        }
    }

    @GetMapping("/history")
    public String bookingHistory(Principal principal, Model model) {
        List<Booking> bookings = bookingService.getBookingsByAccount(principal.getName());
        Booking seatedBooking = bookingService.getActiveSeatedBooking(principal.getName());
        model.addAttribute("bookings", bookings);
        model.addAttribute("seatedBooking", seatedBooking);
        model.addAttribute("isSeated", seatedBooking != null);
        model.addAttribute("pageTitle", "Lịch Sử Đặt Bàn");
        return "customer/booking-history";
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable("id") Long id,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn đặt bàn thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn: " + e.getMessage());
        }
        return "redirect:/customer/booking/history";
    }
}
