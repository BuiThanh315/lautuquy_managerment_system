package com.lautuquy.management.exception;

/**
 * Ném ra khi không tìm thấy tài nguyên theo ID hoặc điều kiện tìm kiếm.
 * GlobalExceptionHandler sẽ bắt exception này và chuyển hướng về trang 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super("Không tìm thấy " + resourceName + " với ID: " + id);
    }
}
