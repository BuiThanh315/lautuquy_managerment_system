package com.lautuquy.management.controller.customer;

import com.lautuquy.management.entity.Booking;
import com.lautuquy.management.entity.Dish;
import com.lautuquy.management.entity.Feedback;
import com.lautuquy.management.service.BookingService;
import com.lautuquy.management.service.DishService;
import com.lautuquy.management.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/customer/feedback")
public class CustomerFeedbackController {

    private final FeedbackService feedbackService;
    private final DishService dishService;
    private final BookingService bookingService;

    public CustomerFeedbackController(FeedbackService feedbackService,
                                      DishService dishService,
                                      BookingService bookingService) {
        this.feedbackService = feedbackService;
        this.dishService = dishService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public String viewFeedbackPage(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/auth/login";
        }
        String username = authentication.getName();
        List<Feedback> userFeedbacks = feedbackService.getFeedbacksByCustomer(username);
        List<Dish> availableDishes = dishService.getAllDishes();
        Booking seatedBooking = bookingService.getActiveSeatedBooking(username);

        model.addAttribute("feedbacks", userFeedbacks);
        model.addAttribute("dishes", availableDishes);
        model.addAttribute("isSeated", seatedBooking != null);
        model.addAttribute("seatedBooking", seatedBooking);
        model.addAttribute("pageTitle", "Góp Ý & Phản Hồi — Lẩu Tứ Quý");

        return "customer/feedback";
    }

    @PostMapping("/submit-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitFeedbackAjax(@RequestParam(value = "dishId", required = false) Long dishId,
                                                                 @RequestParam("content") String content,
                                                                 Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            Feedback feedback = feedbackService.createFeedback(authentication.getName(), dishId, content);
            response.put("success", true);
            response.put("message", "Cảm ơn bạn đã gửi phản hồi cho Lẩu Tứ Quý!");
            response.put("feedbackId", feedback.getId());
            response.put("content", feedback.getContent());
            response.put("dishName", feedback.getDish() != null ? feedback.getDish().getName() : "Ý kiến chung");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể gửi phản hồi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
