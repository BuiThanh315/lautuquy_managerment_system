package com.lautuquy.management.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entity mapping bảng `order_items`.
 * Quan trọng: `actualPrice` lưu snapshot giá của món ăn tại thời điểm gọi món (Order).
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "actual_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualPrice;

    public OrderItem() {}

    public OrderItem(Order order, Dish dish, Integer quantity, BigDecimal actualPrice) {
        this.order = order;
        this.dish = dish;
        this.quantity = quantity;
        this.actualPrice = actualPrice;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Dish getDish() { return dish; }
    public void setDish(Dish dish) { this.dish = dish; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getActualPrice() { return actualPrice; }
    public void setActualPrice(BigDecimal actualPrice) { this.actualPrice = actualPrice; }

    public BigDecimal getSubtotal() {
        if (actualPrice == null || quantity == null) return BigDecimal.ZERO;
        return actualPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
