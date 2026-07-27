package com.lautuquy.management.repository;

import com.lautuquy.management.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho Entity Account.
 * Cung cấp các truy vấn CRUD cơ bản và tìm kiếm theo username.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Tìm tài khoản theo username (dùng cho Spring Security loadUserByUsername).
     */
    Optional<Account> findByUsername(String username);

    /**
     * Kiểm tra xem username đã tồn tại chưa (dùng khi đăng ký).
     */
    boolean existsByUsername(String username);

    /**
     * Kiểm tra xem email đã tồn tại chưa (dùng khi đăng ký).
     */
    boolean existsByEmail(String email);
}
