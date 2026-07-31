package com.lautuquy.management.repository;

import com.lautuquy.management.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByAccountIdOrderByCreatedAtDesc(Long accountId);
    List<Booking> findByStatus(Booking.Status status);
    Optional<Booking> findTopByAccountIdAndStatusOrderByCreatedAtDesc(Long accountId, Booking.Status status);

    @Query("SELECT b FROM Booking b LEFT JOIN b.account a WHERE " +
           "LOWER(b.customerPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "(a IS NOT NULL AND (LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
           "ORDER BY b.createdAt DESC")
    List<Booking> searchByPhoneOrEmail(@Param("keyword") String keyword);
}
