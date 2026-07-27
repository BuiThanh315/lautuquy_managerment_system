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
     * Khóa tài khoản theo ID.
     */
    @PostMapping("/{id}/lock")
    public String lockAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            accountService.lockAccount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã khóa tài khoản thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể khóa tài khoản: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    /**
     * Mở khóa tài khoản theo ID.
     */
    @PostMapping("/{id}/unlock")
    public String unlockAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            accountService.unlockAccount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã mở khóa tài khoản thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể mở khóa tài khoản: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }
}
