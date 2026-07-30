package com.lautuquy.management.controller.customer;

import com.lautuquy.management.entity.Account;
import com.lautuquy.management.entity.Category;
import com.lautuquy.management.entity.Dish;
import com.lautuquy.management.entity.Feedback;
import com.lautuquy.management.repository.AccountRepository;
import com.lautuquy.management.service.CategoryService;
import com.lautuquy.management.service.DishService;
import com.lautuquy.management.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class CustomerLandingController {

    private final CategoryService categoryService;
    private final DishService dishService;
    private final FeedbackService feedbackService;
    private final AccountRepository accountRepository;

    public CustomerLandingController(CategoryService categoryService,
                                    DishService dishService,
                                    FeedbackService feedbackService,
                                    AccountRepository accountRepository) {
        this.categoryService = categoryService;
        this.dishService = dishService;
        this.feedbackService = feedbackService;
        this.accountRepository = accountRepository;
    }

    @GetMapping({"/landing", "/customer/landing"})
    public String showLandingPage(Authentication authentication, Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Dish> allDishes = dishService.getAllDishes();

        // Nhóm món ăn theo từng danh mục + tạo slug tiếng Việt
        List<CategoryGroup> categoryGroups = new ArrayList<>();
        for (Category category : categories) {
            List<Dish> categoryDishes = allDishes.stream()
                    .filter(d -> d.getCategory() != null && Objects.equals(d.getCategory().getId(), category.getId()))
                    .collect(Collectors.toList());

            String slug = toSlug(category.getName());
            categoryGroups.add(new CategoryGroup(category, slug, categoryDishes));
        }

        // Lấy thông tin người dùng nếu đã đăng nhập, ngược lại là "Khách hàng"
        String currentUserFullName = "Khách hàng";
        if (authentication != null && authentication.isAuthenticated()) {
            Account account = accountRepository.findByUsername(authentication.getName()).orElse(null);
            if (account != null && account.getFullName() != null && !account.getFullName().trim().isEmpty()) {
                currentUserFullName = account.getFullName();
            } else if (authentication.getName() != null) {
                currentUserFullName = authentication.getName();
            }
        }

        List<Feedback> allFeedbacks = feedbackService.getAllFeedbacks();

        model.addAttribute("categories", categories);
        model.addAttribute("categoryGroups", categoryGroups);
        model.addAttribute("allDishes", allDishes);
        model.addAttribute("feedbacks", allFeedbacks);
        model.addAttribute("currentUserFullName", currentUserFullName);
        model.addAttribute("pageTitle", "Trang Chủ — Nhà Hàng Lẩu Tứ Quý");

        return "customer/landing";
    }

    // Endpoint POST nhận đánh giá trực tiếp từ Landing Page (cho cả khách vãng lai và user)
    @PostMapping("/api/feedback/landing-submit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitLandingFeedback(@RequestParam(value = "dishId", required = false) Long dishId,
                                                                      @RequestParam("content") String content,
                                                                      Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        if (content == null || content.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Vui lòng nhập nội dung đánh giá của bạn.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String username = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : null;
            Feedback feedback = feedbackService.createGuestOrUserFeedback(username, dishId, content);

            String authorName = "Khách hàng";
            if (feedback.getAccount() != null && feedback.getAccount().getFullName() != null) {
                authorName = feedback.getAccount().getFullName();
            }

            String dishName = (feedback.getDish() != null) ? feedback.getDish().getName() : "Phản hồi chung";
            String dateFormatted = feedback.getCreatedAt() != null
                    ? feedback.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "Mới đây";

            response.put("success", true);
            response.put("message", "Cảm ơn bạn đã gửi đánh giá cho Lẩu Tứ Quý!");
            response.put("authorName", authorName);
            response.put("dishName", dishName);
            response.put("content", feedback.getContent());
            response.put("dateFormatted", dateFormatted);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể gửi đánh giá: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Endpoint REST API cho JS Fetch nếu cần (theo Panding Page Spec)
    @GetMapping("/api/dishes")
    @ResponseBody
    public ResponseEntity<List<Dish>> getDishesApi(@RequestParam(required = false) Long categoryId) {
        if (categoryId != null) {
            return ResponseEntity.ok(dishService.getDishesByCategoryId(categoryId));
        }
        return ResponseEntity.ok(dishService.getAllDishes());
    }

    @GetMapping("/api/products")
    @ResponseBody
    public ResponseEntity<List<Dish>> getProductsApi() {
        return ResponseEntity.ok(dishService.getAllDishes());
    }

    /**
     * DTO hỗ trợ nhóm Category + Slug + Danh sách món
     */
    public static class CategoryGroup {
        private Category category;
        private String slug;
        private List<Dish> dishes;

        public CategoryGroup(Category category, String slug, List<Dish> dishes) {
            this.category = category;
            this.slug = slug;
            this.dishes = dishes;
        }

        public Category getCategory() { return category; }
        public String getSlug() { return slug; }
        public List<Dish> getDishes() { return dishes; }
    }

    /**
     * Chuyển đổi chuỗi tiếng Việt thành slug không dấu dùng cho HTML ID / Anchor Link
     */
    public static String toSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "category";
        }
        String temp = input.trim().toLowerCase();
        temp = temp.replace('đ', 'd').replace('Đ', 'd');
        String normalized = Normalizer.normalize(temp, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String slug = pattern.matcher(normalized).replaceAll("");
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.replaceAll("\\s+", "-");
        slug = slug.replaceAll("-+", "-");
        slug = slug.replaceAll("^-|-$", "");
        return slug.isEmpty() ? "category" : slug;
    }
}
