package com.lautuquy.management.service.impl;

import com.lautuquy.management.entity.*;
import com.lautuquy.management.exception.ResourceNotFoundException;
import com.lautuquy.management.repository.*;
import com.lautuquy.management.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookingRepository bookingRepository;
    private final BookingPreorderRepository bookingPreorderRepository;
    private final DishRepository dishRepository;
    private final RestaurantTableRepository restaurantTableRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            BookingRepository bookingRepository,
                            BookingPreorderRepository bookingPreorderRepository,
                            DishRepository dishRepository,
                            RestaurantTableRepository restaurantTableRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.bookingRepository = bookingRepository;
        this.bookingPreorderRepository = bookingPreorderRepository;
        this.dishRepository = dishRepository;
        this.restaurantTableRepository = restaurantTableRepository;
    }

    @Override
    @Transactional
    public Order createOrder(Long bookingId, Order.OrderType orderType) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn đặt bàn", bookingId));

        RestaurantTable table = booking.getAssignedTable();
        if (table == null) {
            List<RestaurantTable> tables = restaurantTableRepository.findByTableTypeId(booking.getTableType().getId());
            table = tables.stream()
                    .filter(t -> t.getStatus() == RestaurantTable.Status.EMPTY || t.getStatus() == RestaurantTable.Status.RESERVED)
                    .findFirst()
                    .orElseGet(() -> restaurantTableRepository.findAll().stream()
                            .filter(t -> t.getStatus() == RestaurantTable.Status.EMPTY || t.getStatus() == RestaurantTable.Status.RESERVED)
                            .findFirst()
                            .orElseGet(() -> restaurantTableRepository.findAll().stream().findFirst()
                                    .orElseThrow(() -> new IllegalStateException("Không có bàn ăn nào trong hệ thống."))));

            booking.setAssignedTable(table);
            table.setStatus(RestaurantTable.Status.SERVING);
            restaurantTableRepository.save(table);
            bookingRepository.save(booking);
        }

        Order order = new Order(booking, orderType != null ? orderType : Order.OrderType.DINE_IN);
        order.setAccount(booking.getAccount());
        order.setTable(table);
        Order savedOrder = orderRepository.save(order);

        // Chuyển các món đặt trước (BookingPreorders) vào OrderItems nếu có
        List<BookingPreorder> preorders = bookingPreorderRepository.findByBookingId(bookingId);
        for (BookingPreorder preorder : preorders) {
            Dish dish = preorder.getDish();
            if (dish != null && preorder.getQuantity() != null && preorder.getQuantity() > 0) {
                // Snapshot actualPrice = dish.price lúc này
                OrderItem item = new OrderItem(savedOrder, dish, preorder.getQuantity(), dish.getPrice());
                orderItemRepository.save(item);
            }
        }

        return savedOrder;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn gọi món (Order)", id));
    }

    @Override
    @Transactional
    public Order getOrCreateActiveOrderForBooking(Long bookingId) {
        Optional<Order> activeOrderOpt = orderRepository.findFirstByBookingIdAndStatus(bookingId, Order.Status.PROCESSING);
        if (activeOrderOpt.isPresent()) {
            return activeOrderOpt.get();
        }
        return createOrder(bookingId, Order.OrderType.DINE_IN);
    }

    @Override
    public List<Order> getOrdersByBookingId(Long bookingId) {
        return orderRepository.findByBookingId(bookingId);
    }

    @Override
    @Transactional
    public OrderItem addItem(Long orderId, Long dishId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        }

        Order order = getOrderById(orderId);
        if (order.getStatus() != Order.Status.PROCESSING) {
            throw new IllegalStateException("Không thể thêm món vào đơn gọi đã hoàn thành hoặc bị hủy.");
        }

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn", dishId));

        if (dish.getStatus() == Dish.Status.OUT_OF_STOCK || dish.getQuantity() == null || dish.getQuantity() < 1) {
            throw new IllegalArgumentException("Món '" + dish.getName() + "' hiện đang tạm hết hàng.");
        }

        if (dish.getQuantity() < quantity) {
            throw new IllegalArgumentException("Món '" + dish.getName() + "' chỉ còn lại " + dish.getQuantity() + " suất.");
        }

        // Trừ số lượng món trong kho
        dish.setQuantity(dish.getQuantity() - quantity);
        if (dish.getQuantity() < 1) {
            dish.setStatus(Dish.Status.OUT_OF_STOCK);
        }
        dishRepository.save(dish);

        // Kiểm tra xem món đã có trong OrderItem chưa
        Optional<OrderItem> existingItemOpt = orderItemRepository.findByOrderIdAndDishId(orderId, dishId);
        if (existingItemOpt.isPresent()) {
            OrderItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            return orderItemRepository.save(existingItem);
        } else {
            // SNAPSHOT giá thực tế tại thời điểm gọi món
            OrderItem newItem = new OrderItem(order, dish, quantity, dish.getPrice());
            return orderItemRepository.save(newItem);
        }
    }

    @Override
    @Transactional
    public OrderItem updateItem(Long orderItemId, int quantity) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Món trong đơn", orderItemId));

        if (item.getOrder().getStatus() != Order.Status.PROCESSING) {
            throw new IllegalStateException("Không thể sửa đơn gọi món đã hoàn thành hoặc bị hủy.");
        }

        Dish dish = item.getDish();
        if (quantity <= 0) {
            if (dish != null) {
                dish.setQuantity(dish.getQuantity() + item.getQuantity());
                if (dish.getQuantity() >= 1 && dish.getStatus() == Dish.Status.OUT_OF_STOCK) {
                    dish.setStatus(Dish.Status.AVAILABLE);
                }
                dishRepository.save(dish);
            }
            if (item.getOrder() != null) {
                if (item.getOrder().getOrderItems() != null) {
                    item.getOrder().getOrderItems().remove(item);
                }
                if (item.getOrder().getBooking() != null && dish != null) {
                    bookingPreorderRepository.deleteByBookingIdAndDishId(item.getOrder().getBooking().getId(), dish.getId());
                }
            }
            orderItemRepository.delete(item);
            orderItemRepository.flush();
            return null;
        } else {
            int diff = quantity - item.getQuantity();
            if (diff > 0) {
                if (dish.getQuantity() < diff) {
                    throw new IllegalArgumentException("Món '" + dish.getName() + "' chỉ còn lại " + dish.getQuantity() + " suất.");
                }
                dish.setQuantity(dish.getQuantity() - diff);
                if (dish.getQuantity() < 1) {
                    dish.setStatus(Dish.Status.OUT_OF_STOCK);
                }
                dishRepository.save(dish);
            } else if (diff < 0) {
                dish.setQuantity(dish.getQuantity() + Math.abs(diff));
                if (dish.getQuantity() >= 1 && dish.getStatus() == Dish.Status.OUT_OF_STOCK) {
                    dish.setStatus(Dish.Status.AVAILABLE);
                }
                dishRepository.save(dish);
            }

            item.setQuantity(quantity);
            return orderItemRepository.save(item);
        }
    }

    @Override
    @Transactional
    public void removeItem(Long orderItemId) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Món trong đơn", orderItemId));

        if (item.getOrder().getStatus() != Order.Status.PROCESSING) {
            throw new IllegalStateException("Không thể xóa món khỏi đơn gọi đã hoàn thành hoặc bị hủy.");
        }

        Dish dish = item.getDish();
        if (dish != null) {
            dish.setQuantity(dish.getQuantity() + item.getQuantity());
            if (dish.getQuantity() >= 1 && dish.getStatus() == Dish.Status.OUT_OF_STOCK) {
                dish.setStatus(Dish.Status.AVAILABLE);
            }
            dishRepository.save(dish);
        }

        if (item.getOrder() != null) {
            if (item.getOrder().getOrderItems() != null) {
                item.getOrder().getOrderItems().remove(item);
            }
            if (item.getOrder().getBooking() != null && dish != null) {
                bookingPreorderRepository.deleteByBookingIdAndDishId(item.getOrder().getBooking().getId(), dish.getId());
            }
        }
        orderItemRepository.delete(item);
        orderItemRepository.flush();
    }
}
