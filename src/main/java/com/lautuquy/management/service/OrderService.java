package com.lautuquy.management.service;

import com.lautuquy.management.entity.Order;
import com.lautuquy.management.entity.OrderItem;

import java.util.List;

public interface OrderService {
    Order createOrder(Long bookingId, Order.OrderType orderType);
    Order getOrderById(Long id);
    Order getOrCreateActiveOrderForBooking(Long bookingId);
    List<Order> getOrdersByBookingId(Long bookingId);
    OrderItem addItem(Long orderId, Long dishId, int quantity);
    OrderItem updateItem(Long orderItemId, int quantity);
    void removeItem(Long orderItemId);
}
