package com.lautuquy.management.repository;

import com.lautuquy.management.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    Optional<OrderItem> findByOrderIdAndDishId(Long orderId, Long dishId);
    void deleteByOrderId(Long orderId);
}
