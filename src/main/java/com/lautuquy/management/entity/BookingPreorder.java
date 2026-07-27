package com.lautuquy.management.entity;

import jakarta.persistence.*;

/**
 * Entity mapping bảng `booking_preorders` (món đặt trước khi đặt bàn).
 */
@Entity
@Table(name = "booking_preorders")
public class BookingPreorder {

    @EmbeddedId
    private BookingPreorderId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookingId")
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("dishId")
    @JoinColumn(name = "dish_id")
    private Dish dish;

    @Column(nullable = false)
    private Integer quantity = 1;

    public BookingPreorder() {}

    public BookingPreorder(Booking booking, Dish dish, Integer quantity) {
        this.id = new BookingPreorderId(booking.getId(), dish.getId());
        this.booking = booking;
        this.dish = dish;
        this.quantity = quantity;
    }

    public BookingPreorderId getId() { return id; }
    public void setId(BookingPreorderId id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public Dish getDish() { return dish; }
    public void setDish(Dish dish) { this.dish = dish; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
