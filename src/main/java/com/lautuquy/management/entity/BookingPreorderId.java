package com.lautuquy.management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Composite Primary Key cho bảng `booking_preorders`.
 */
@Embeddable
public class BookingPreorderId implements Serializable {

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "dish_id")
    private Long dishId;

    public BookingPreorderId() {}

    public BookingPreorderId(Long bookingId, Long dishId) {
        this.bookingId = bookingId;
        this.dishId = dishId;
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingPreorderId that = (BookingPreorderId) o;
        return Objects.equals(bookingId, that.bookingId) && Objects.equals(dishId, that.dishId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId, dishId);
    }
}
