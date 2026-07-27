package com.lautuquy.management.repository;

import com.lautuquy.management.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByOrderId(Long orderId);

    Optional<Invoice> findByOrderBookingId(Long bookingId);

    List<Invoice> findByPaymentStatusOrderByCreatedAtDesc(Invoice.PaymentStatus paymentStatus);

    List<Invoice> findAllByOrderByCreatedAtDesc();

    @Query("SELECT SUM(i.finalAmount) FROM Invoice i WHERE i.paymentStatus = :status")
    BigDecimal sumTotalRevenueByPaymentStatus(@Param("status") Invoice.PaymentStatus status);

    @Query("SELECT SUM(i.finalAmount) FROM Invoice i WHERE i.paymentStatus = :status AND i.createdAt >= :start AND i.createdAt <= :end")
    BigDecimal sumRevenueBetween(@Param("status") Invoice.PaymentStatus status,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    List<Invoice> findByPaymentStatusAndCreatedAtBetween(Invoice.PaymentStatus status, LocalDateTime start, LocalDateTime end);
}
