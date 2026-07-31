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

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN b.account a WHERE " +
           "LOWER(b.customerPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "(b.customerEmail IS NOT NULL AND LOWER(b.customerEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
           "LOWER(b.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "(a IS NOT NULL AND (" +
           "   LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "   LOWER(a.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "   LOWER(a.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
           ")) OR " +
           "(b.customerPhone IS NOT NULL AND b.customerPhone != '' AND b.customerPhone IN (" +
           "   SELECT acc.phone FROM Account acc WHERE LOWER(acc.email) LIKE LOWER(CONCAT('%', :keyword, '%')) AND acc.phone IS NOT NULL AND acc.phone != ''" +
           ")) OR " +
           "(b.customerEmail IS NOT NULL AND b.customerEmail != '' AND b.customerEmail IN (" +
           "   SELECT acc.email FROM Account acc WHERE LOWER(acc.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) AND acc.email IS NOT NULL AND acc.email != ''" +
           ")) OR " +
           "(a IS NOT NULL AND a.email IS NOT NULL AND a.email != '' AND a.email IN (" +
           "   SELECT acc2.email FROM Account acc2 WHERE LOWER(acc2.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) AND acc2.email IS NOT NULL AND acc2.email != ''" +
           ")) " +
           "ORDER BY b.createdAt DESC")
    List<Booking> searchByPhoneOrEmail(@Param("keyword") String keyword);
}
