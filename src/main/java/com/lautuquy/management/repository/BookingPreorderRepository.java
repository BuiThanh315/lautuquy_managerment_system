package com.lautuquy.management.repository;

import com.lautuquy.management.entity.BookingPreorder;
import com.lautuquy.management.entity.BookingPreorderId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingPreorderRepository extends JpaRepository<BookingPreorder, BookingPreorderId> {
    List<BookingPreorder> findByBookingId(Long bookingId);
}
