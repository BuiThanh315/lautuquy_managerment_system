package com.lautuquy.management.exception;

/**
 * Ném ra khi xảy ra xung đột đặt bàn (ví dụ: bàn đã được đặt trong khung giờ đó).
 * Sẽ được sử dụng trong Giai đoạn 2 — BookingServiceImpl.
 */
public class BookingConflictException extends RuntimeException {

    public BookingConflictException(String message) {
        super(message);
    }
}
