package com.lautuquy.management.controller.admin;

import com.lautuquy.management.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller Admin quản lý tài khoản người dùng.
 * Phân quyền: chỉ ROLE_ADMIN mới được truy cập (đã cấu hình trong SecurityConfig).
 */
@Controller
@RequestMapping("/admin/accounts")
public class AccountAdminController {

    private final AccountService accountService;

    public AccountAdminController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Danh sách tất cả tài khoản trong hệ thống.
     */
    @GetMapping
    public String listAccounts(Model model) {
        model.addAttribute("accounts", accountService.getAllAccounts());
        model.addAttribute("pageTitle", "Quản lý Tài khoản");
        return "admin/accounts";
    }

    /**
     * Khóa tài khoản theo ID (Xử lý AJAX).
     */
    @PostMapping("/{id}/lock")
    @ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> lockAccount(@PathVariable Long id) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            accountService.lockAccount(id);
            response.put("success", true);
            response.put("message", "Đã khóa tài khoản thành công.");
            response.put("status", "LOCKED");
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể khóa tài khoản: " + e.getMessage());
            return org.springframework.http.ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Mở khóa tài khoản theo ID (Xử lý AJAX).
     */
    @PostMapping("/{id}/unlock")
    @ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> unlockAccount(@PathVariable Long id) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            accountService.unlockAccount(id);
            response.put("success", true);
            response.put("message", "Đã mở khóa tài khoản thành công.");
            response.put("status", "ACTIVE");
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể mở khóa tài khoản: " + e.getMessage());
            return org.springframework.http.ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Khóa hàng loạt tài khoản theo danh sách ID (Xử lý AJAX).
     */
    @PostMapping("/bulk-lock")
    @ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> bulkLockAccounts(
            @RequestBody java.util.List<Long> ids,
            java.security.Principal principal) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            int count = 0;
            if (ids != null) {
                for (Long id : ids) {
                    var acc = accountService.findById(id);
                    // Không cho phép tự khóa chính tài khoản admin đang thao tác
                    if (principal != null && acc.getUsername().equals(principal.getName()) && acc.getRole() == com.lautuquy.management.entity.Account.Role.ADMIN) {
                        continue;
                    }
                    accountService.lockAccount(id);
                    count++;
                }
            }
            response.put("success", true);
            response.put("message", "Đã khóa thành công " + count + " tài khoản.");
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể khóa hàng loạt: " + e.getMessage());
            return org.springframework.http.ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Mở khóa hàng loạt tài khoản theo danh sách ID (Xử lý AJAX).
     */
    @PostMapping("/bulk-unlock")
    @ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> bulkUnlockAccounts(
            @RequestBody java.util.List<Long> ids) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            int count = 0;
            if (ids != null) {
                for (Long id : ids) {
                    accountService.unlockAccount(id);
                    count++;
                }
            }
            response.put("success", true);
            response.put("message", "Đã mở khóa thành công " + count + " tài khoản.");
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể mở khóa hàng loạt: " + e.getMessage());
            return org.springframework.http.ResponseEntity.badRequest().body(response);
        }
    }
}
