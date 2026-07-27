package com.lautuquy.management.exception;

/**
 * Ném ra khi tài khoản cố truy cập tài nguyên ngoài phạm vi quyền hạn.
 * Thường được sử dụng trong tầng Service để kiểm tra quyền truy cập nghiệp vụ.
 */
public class InsufficientPermissionException extends RuntimeException {

    public InsufficientPermissionException(String message) {
        super(message);
    }
}
