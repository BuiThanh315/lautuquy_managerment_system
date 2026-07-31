package com.lautuquy.management.controller.customer;

import com.lautuquy.management.dto.request.BookingRequest;
import com.lautuquy.management.entity.Booking;
import com.lautuquy.management.entity.Dish;
import com.lautuquy.management.service.AccountService;
import com.lautuquy.management.service.BookingService;
import com.lautuquy.management.service.DishService;
import com.lautuquy.management.service.TableService;
import jakarta.validation.Valid;
import com.lautuquy.management.dto.response.BookingDetailDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/customer")
public class BookingController {

    private final BookingService bookingService;
    private final TableService tableService;
    private final DishService dishService;
    private final AccountService accountService;
    private final com.lautuquy.management.service.TableLockService tableLockService;

    public BookingController(BookingService bookingService,
                             TableService tableService,
                             DishService dishService,
                             AccountService accountService,
                             com.lautuquy.management.service.TableLockService tableLockService) {
        this.bookingService = bookingService;
        this.tableService = tableService;
        this.dishService = dishService;
        this.accountService = accountService;
        this.tableLockService = tableLockService;
    }

    @GetMapping("/booking")
    public String showBookingForm(Model model, Principal principal) {
        BookingRequest bookingRequest = new BookingRequest();
        Booking seatedBooking = null;
        if (principal != null) {
            var account = accountService.loadUserByUsername(principal.getName());
            if (account instanceof com.lautuquy.management.entity.Account acc) {
                bookingRequest.setCustomerName(acc.getFullName());
                bookingRequest.setCustomerPhone(acc.getPhone());
                bookingRequest.setCustomerEmail(acc.getEmail());
            }
            seatedBooking = bookingService.getActiveSeatedBooking(principal.getName());
        }

        if (seatedBooking != null) {
            return "redirect:/customer/order";
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

    @PostMapping("/booking")
    public String processBooking(@Valid @ModelAttribute("bookingRequest") BookingRequest request,
                                 BindingResult bindingResult,
                                 Principal principal,
                                 jakarta.servlet.http.HttpSession session,
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
            String username = (principal != null) ? principal.getName() : null;
            Booking booking = bookingService.createBooking(username, request);
            tableLockService.releaseSessionLock(session.getId());
            
            redirectAttributes.addFlashAttribute("bookingSuccess", true);
            redirectAttributes.addFlashAttribute("createdBookingId", booking.getId());

            if (principal == null) {
                redirectAttributes.addFlashAttribute("successMessage", "Đặt bàn thành công! Lễ tân Lẩu Tứ Quý sẽ liên hệ xác nhận qua SĐT " + request.getCustomerPhone());
                return "redirect:/customer/booking";
            }
            redirectAttributes.addFlashAttribute("successMessage", "Đặt bàn thành công! Đơn của bạn đang chờ nhà hàng tiếp nhận.");
            return "redirect:/customer/booking";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Không thể tạo đơn đặt bàn: " + e.getMessage());
            model.addAttribute("tableTypes", tableService.getAllTableTypes());
            List<Dish> availableDishes = dishService.getAllDishes();
            model.addAttribute("availableDishes", availableDishes);
            return "customer/booking-form";
        }
    }

    @PostMapping("/booking/api-submit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitBookingApi(
            @Valid @ModelAttribute("bookingRequest") BookingRequest request,
            BindingResult bindingResult,
            Principal principal,
            jakarta.servlet.http.HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (principal != null) {
            Booking seatedBooking = bookingService.getActiveSeatedBooking(principal.getName());
            if (seatedBooking != null) {
                response.put("success", false);
                response.put("message", "Bạn đang ngồi tại bàn ăn (" + (seatedBooking.getAssignedTable() != null ? "Bàn " + seatedBooking.getAssignedTable().getTableNumber() : "Bàn ăn") + "). Không thể tạo thêm đơn đặt bàn mới khi chưa hoàn tất!");
                return ResponseEntity.badRequest().body(response);
            }
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (request.getBookingDate() != null && request.getBookingDate().isBefore(today)) {
            bindingResult.rejectValue("bookingDate", "error.bookingDate", "Ngày đặt bàn không thể ở trong quá khứ.");
        }
        if (request.getBookingDate() != null && request.getBookingDate().isEqual(today)) {
            if (request.getBookingTime() != null && request.getBookingTime().isBefore(now)) {
                bindingResult.rejectValue("bookingTime", "error.bookingTime", "Giờ đặt bàn không thể trước thời gian hiện tại.");
            }
        }

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().stream()
                    .map(org.springframework.context.MessageSourceResolvable::getDefaultMessage)
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Dữ liệu nhập vào không hợp lệ.");
            response.put("success", false);
            response.put("message", errorMsg);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String username = (principal != null) ? principal.getName() : null;
            Booking savedBooking = bookingService.createBooking(username, request);
            tableLockService.releaseSessionLock(session.getId());

            BookingDetailDto detail = bookingService.getBookingDetail(savedBooking.getId());

            response.put("success", true);
            response.put("bookingId", savedBooking.getId());
            response.put("message", "Đặt bàn thành công!");
            response.put("bookingDetail", detail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể tạo đơn đặt bàn: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping({"/booking/history", "/booking-history"})
    public String bookingHistory(@RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
                                 Principal principal,
                                 jakarta.servlet.http.HttpServletRequest request,
                                 Model model) {
        request.getSession(true);
        List<Booking> bookings;
        if (keyword != null && !keyword.trim().isEmpty()) {
            bookings = bookingService.searchBookingsByPhoneOrEmail(keyword.trim());
            model.addAttribute("keyword", keyword.trim());
            if (bookings.isEmpty()) {
                model.addAttribute("infoMessage", "Không tìm thấy đơn đặt bàn nào với thông tin \"" + keyword.trim() + "\" hoặc bạn đã nhập sai thông tin tìm kiếm. Vui lòng kiểm tra lại Số điện thoại hoặc Email.");
            }
        } else if (principal != null) {
            bookings = bookingService.getBookingsByAccount(principal.getName());
        } else {
            bookings = java.util.Collections.emptyList();
            model.addAttribute("infoMessage", "Vui lòng nhập Số điện thoại hoặc Email để tìm kiếm đơn đặt bàn.");
        }

        Booking seatedBooking = (principal != null) ? bookingService.getActiveSeatedBooking(principal.getName()) : null;
        model.addAttribute("bookings", bookings);
        model.addAttribute("seatedBooking", seatedBooking);
        model.addAttribute("isSeated", seatedBooking != null);
        model.addAttribute("pageTitle", "Lịch Sử Đặt Bàn");

        if ("XMLHttpRequest".equals(requestedWith)) {
            return "customer/booking-history :: bookingListSection";
        }

        return "customer/booking-history";
    }

    @GetMapping({"/booking/api/detail/{id}", "/booking-history/api/detail/{id}"})
    @ResponseBody
    public ResponseEntity<BookingDetailDto> getBookingDetailApi(@PathVariable("id") Long id) {
        try {
            BookingDetailDto detail = bookingService.getBookingDetail(id);
            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping({"/booking/{id}/cancel", "/booking-history/{id}/cancel", "/booking/history/{id}/cancel"})
    public String cancelBooking(@PathVariable("id") Long id,
                                @RequestParam(value = "keyword", required = false) String keyword,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            String username = (principal != null) ? principal.getName() : null;
            bookingService.cancelBooking(id, username);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn đặt bàn thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn: " + e.getMessage());
        }
        if (keyword != null && !keyword.isBlank()) {
            return "redirect:/customer/booking-history?keyword=" + java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:/customer/booking-history";
    }
}
