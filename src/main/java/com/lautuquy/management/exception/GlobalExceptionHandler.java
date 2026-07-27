package com.lautuquy.management.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Xử lý tập trung toàn bộ Exception trong ứng dụng.
 * Trả về trang HTML thân thiện, KHÔNG để lộ StackTrace ra giao diện người dùng.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi 403 — Truy cập bị từ chối (không đủ quyền).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex,
                                      Model model,
                                      HttpServletRequest request) {
        model.addAttribute("requestUrl", request.getRequestURI());
        model.addAttribute("message", "Bạn không có quyền truy cập vào trang này.");
        return "error/403";
    }

    /**
     * Xử lý lỗi khi tài khoản bị khóa (status = LOCKED).
     */
    @ExceptionHandler(DisabledException.class)
    public String handleDisabled(DisabledException ex, Model model) {
        model.addAttribute("message", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
        return "error/403";
    }

    /**
     * Xử lý lỗi 404 — Không tìm thấy tài nguyên.
     */
    @ExceptionHandler({ResourceNotFoundException.class,
                        NoHandlerFoundException.class,
                        NoResourceFoundException.class})
    public String handleNotFound(Exception ex, Model model, HttpServletRequest request) {
        model.addAttribute("requestUrl", request.getRequestURI());
        model.addAttribute("message", "Trang hoặc tài nguyên bạn tìm kiếm không tồn tại.");
        return "error/404";
    }

    /**
     * Xử lý lỗi nghiệp vụ — dữ liệu không hợp lệ từ request của người dùng.
     */
    @ExceptionHandler({IllegalArgumentException.class, BookingConflictException.class})
    public String handleBadRequest(RuntimeException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/400";
    }

    /**
     * Xử lý lỗi không quyền truy cập từ tầng nghiệp vụ.
     */
    @ExceptionHandler(InsufficientPermissionException.class)
    public String handleInsufficientPermission(InsufficientPermissionException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/403";
    }

    /**
     * Bắt tất cả các Exception không được xử lý — lỗi 500 server.
     * Tuyệt đối không hiển thị StackTrace ra ngoài.
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model, HttpServletRequest request) {
        // Log lỗi chi tiết ở server-side (không expose ra client)
        System.err.println("[ERROR] Unhandled exception at " + request.getRequestURI() + ": " + ex.getMessage());
        ex.printStackTrace();

        model.addAttribute("message", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau hoặc liên hệ hỗ trợ.");
        return "error/500";
    }
}
