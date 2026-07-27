package com.lautuquy.management.repository;

import com.lautuquy.management.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBookingId(Long bookingId);
    List<Order> findByBookingIdAndStatus(Long bookingId, Order.Status status);
    Optional<Order> findFirstByBookingIdAndStatus(Long bookingId, Order.Status status);
}
