package com.lautuquy.management.service.impl;

import com.lautuquy.management.dto.request.RegisterRequest;
import com.lautuquy.management.entity.Account;
import com.lautuquy.management.exception.ResourceNotFoundException;
import com.lautuquy.management.repository.AccountRepository;
import com.lautuquy.management.service.AccountService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Triển khai AccountService.
 * - loadUserByUsername: tích hợp Spring Security, kiểm tra tài khoản LOCKED.
 * - registerAccount: đăng ký tài khoản mới vai trò CUSTOMER.
 * - lockAccount / unlockAccount: Admin quản lý trạng thái.
 * - changePassword: đổi mật khẩu sau xác minh.
 */
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountServiceImpl(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Được gọi bởi Spring Security trong quá trình xác thực.
     * Khi tài khoản có status = LOCKED, isEnabled() = false → Spring Security ném DisabledException.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy tài khoản: " + username
                ));
    }

    /**
     * Đăng ký tài khoản mới.
     * Tự động mã hóa mật khẩu bằng BCrypt, gán role CUSTOMER và status ACTIVE.
     */
    @Override
    @Transactional
    public Account registerAccount(RegisterRequest request) {
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập '" + request.getUsername() + "' đã được sử dụng.");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && accountRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' đã được đăng ký.");
        }

        Account account = Account.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(Account.Role.CUSTOMER)
                .status(Account.Status.ACTIVE)
                .build();

        return accountRepository.save(account);
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public Account findById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + id));
    }

    /**
     * Khóa tài khoản — chỉ Admin có quyền thực hiện.
     */
    @Override
    @Transactional
    public void lockAccount(Long id) {
        Account account = findById(id);
        account.setStatus(Account.Status.LOCKED);
        accountRepository.save(account);
    }

    /**
     * Mở khóa tài khoản — chỉ Admin có quyền thực hiện.
     */
    @Override
    @Transactional
    public void unlockAccount(Long id) {
        Account account = findById(id);
        account.setStatus(Account.Status.ACTIVE);
        accountRepository.save(account);
    }

    /**
     * Đổi mật khẩu: xác minh mật khẩu cũ trước khi lưu mật khẩu mới đã mã hóa.
     */
    @Override
    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {
        Account account = findById(id);

        if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác.");
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }
}
