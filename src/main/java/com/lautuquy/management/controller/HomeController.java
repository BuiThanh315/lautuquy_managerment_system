package com.lautuquy.management.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller xử lý route gốc `/`.
 * Tự động chuyển hướng người dùng chưa đăng nhập về `/auth/login`
 * và người dùng đã đăng nhập về Dashboard theo vai trò.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            var authorities = authentication.getAuthorities();
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/admin/accounts";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"))) {
                return "redirect:/staff/bookings";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
                return "redirect:/customer/menu";
            }
        }
        return "redirect:/auth/login";
    }
}
