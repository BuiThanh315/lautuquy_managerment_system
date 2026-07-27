package com.lautuquy.management.service;

import com.lautuquy.management.dto.request.RegisterRequest;
import com.lautuquy.management.entity.Account;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

/**
 * Interface Service cho nghiệp vụ quản lý tài khoản.
 * Extends UserDetailsService để tích hợp với Spring Security authentication.
 */
public interface AccountService extends UserDetailsService {

    /**
     * Đăng ký tài khoản mới với vai trò CUSTOMER.
     * Ném IllegalArgumentException nếu username hoặc email đã tồn tại.
     */
    Account registerAccount(RegisterRequest request);

    /**
     * Lấy danh sách tất cả tài khoản (dành cho Admin).
     */
    List<Account> getAllAccounts();

    /**
     * Tìm tài khoản theo ID. Ném ResourceNotFoundException nếu không tìm thấy.
     */
    Account findById(Long id);

    /**
     * Khóa tài khoản (đặt status = LOCKED). Dành cho Admin.
     */
    void lockAccount(Long id);

    /**
     * Mở khóa tài khoản (đặt status = ACTIVE). Dành cho Admin.
     */
    void unlockAccount(Long id);

    /**
     * Đổi mật khẩu sau khi xác minh mật khẩu cũ.
     * Ném IllegalArgumentException nếu mật khẩu cũ không khớp.
     */
    void changePassword(Long id, String oldPassword, String newPassword);
}
