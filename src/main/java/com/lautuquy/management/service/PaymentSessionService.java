package com.lautuquy.management.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service quản lý các phiên xác nhận thanh toán tạm thời (In-Memory).
 * Không truy vấn CSDL freedb.tech giúp tốc độ cực nhanh và tránh làm quá tải Connection Pool.
 */
@Service
public class PaymentSessionService {

    public static class PaymentSessionInfo {
        private final String sessionId;
        private volatile boolean confirmed;
        private final long createdAt;

        public PaymentSessionInfo(String sessionId) {
            this.sessionId = sessionId;
            this.confirmed = false;
            this.createdAt = System.currentTimeMillis();
        }

        public String getSessionId() {
            return sessionId;
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public void setConfirmed(boolean confirmed) {
            this.confirmed = confirmed;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }

    private final Map<String, PaymentSessionInfo> sessionMap = new ConcurrentHashMap<>();

    /**
     * Tạo một session ID mới và lưu vào RAM server.
     */
    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        sessionMap.put(sessionId, new PaymentSessionInfo(sessionId));
        return sessionId;
    }

    /**
     * Xác nhận thanh toán cho một session ID từ điện thoại.
     */
    public boolean confirmSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }
        PaymentSessionInfo info = sessionMap.get(sessionId);
        if (info == null) {
            // Nếu chưa có, tạo mới và đánh dấu đã xác nhận
            info = new PaymentSessionInfo(sessionId);
            sessionMap.put(sessionId, info);
        }
        info.setConfirmed(true);
        return true;
    }

    /**
     * Kiểm tra xem session ID đã được xác nhận thanh toán chưa (dùng cho polling trên PC).
     */
    public boolean isSessionConfirmed(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }
        PaymentSessionInfo info = sessionMap.get(sessionId);
        return info != null && info.isConfirmed();
    }

    /**
     * Tự động làm sạch các session đã quá 30 phút (chạy định kỳ 5 phút 1 lần).
     */
    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        long maxAge = 30 * 60 * 1000L; // 30 phút
        sessionMap.entrySet().removeIf(entry -> (now - entry.getValue().getCreatedAt()) > maxAge);
    }
}
