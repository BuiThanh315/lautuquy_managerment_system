package com.lautuquy.management.service;

import com.lautuquy.management.dto.request.BookingRequest;
import com.lautuquy.management.entity.Booking;
import com.lautuquy.management.entity.BookingPreorder;

import com.lautuquy.management.dto.response.BookingDetailDto;

import java.util.List;

public interface BookingService {
    Booking createBooking(String username, BookingRequest request);
    List<Booking> getBookingsByAccount(String username);
    List<Booking> searchBookingsByPhoneOrEmail(String keyword);
    List<Booking> getAllBookings();
    Booking getBookingById(Long id);
    BookingDetailDto getBookingDetail(Long id);
    List<BookingPreorder> getPreordersByBookingId(Long bookingId);
    void cancelBooking(Long id, String username);
    void confirmBooking(Long id);
    void receiveBooking(Long id);
    void seatBooking(Long id, Long tableId);
    void cancelBookingByStaff(Long id);
    Booking getActiveSeatedBooking(String username);
}
