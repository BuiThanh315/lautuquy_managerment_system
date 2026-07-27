package com.lautuquy.management.controller.customer;

import com.lautuquy.management.entity.Booking;
import com.lautuquy.management.entity.Invoice;
import com.lautuquy.management.entity.Order;
import com.lautuquy.management.entity.OrderItem;
import com.lautuquy.management.repository.OrderItemRepository;
import com.lautuquy.management.service.BookingService;
import com.lautuquy.management.service.InvoiceService;
import com.lautuquy.management.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/customer/payment")
public class CustomerPaymentController {

    private final BookingService bookingService;
    private final OrderService orderService;
    private final InvoiceService invoiceService;
    private final OrderItemRepository orderItemRepository;

    public CustomerPaymentController(BookingService bookingService,
                                     OrderService orderService,
                                     InvoiceService invoiceService,
                                     OrderItemRepository orderItemRepository) {
        this.bookingService = bookingService;
        this.orderService = orderService;
        this.invoiceService = invoiceService;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * Hiển thị trang Thanh toán dành cho Khách hàng tại bàn.
     */
    @GetMapping
    public String viewCustomerPayment(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/auth/login";
        }
        String username = authentication.getName();
        Booking seatedBooking = bookingService.getActiveSeatedBooking(username);

        if (seatedBooking == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn chưa được nhận bàn tại nhà hàng để thực hiện thanh toán.");
            return "redirect:/customer/menu";
        }

        Order activeOrder = orderService.getOrCreateActiveOrderForBooking(seatedBooking.getId());
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(activeOrder.getId());
        BigDecimal totalAmount = orderItems.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        Invoice invoice = invoiceService.getInvoiceByBookingId(seatedBooking.getId());

        model.addAttribute("seatedBooking", seatedBooking);
        model.addAttribute("activeOrder", activeOrder);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("invoice", invoice);
        model.addAttribute("isSeated", true);
        model.addAttribute("pageTitle", "Thanh Toán Đơn Bàn — Lẩu Tứ Quý");

        return "customer/payment";
    }

    /**
     * Khách hàng gửi yêu cầu thanh toán (chọn Tiền mặt hoặc Chuyển khoản).
     */
    @PostMapping("/submit")
    public String submitPaymentRequest(@RequestParam("paymentMethod") Invoice.PaymentMethod paymentMethod,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/auth/login";
        }
        try {
            Invoice invoice = invoiceService.requestPaymentByCustomer(authentication.getName(), paymentMethod);
            redirectAttributes.addFlashAttribute("successMessage", "Thanh toán thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Thao tác thanh toán không thành công: " + e.getMessage());
        }
        return "redirect:/customer/payment";
    }
}
