package com.lautuquy.management.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entity mapping bảng `dishes`.
 */
@Entity
@Table(name = "dishes")
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal price;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Integer quantity = 50;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Status status = Status.AVAILABLE;

    public enum Status {
        AVAILABLE, OUT_OF_STOCK
    }

    public Dish() {}

    public Dish(Long id, Category category, String name, String imageUrl, BigDecimal price, String description, Integer quantity, Status status) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.description = description;
        this.quantity = (quantity != null) ? quantity : 0;
        this.status = (this.quantity < 1) ? Status.OUT_OF_STOCK : (status != null ? status : Status.AVAILABLE);
    }

    @PrePersist
    @PreUpdate
    protected void checkQuantityAndStatus() {
        if (this.quantity == null || this.quantity < 1) {
            this.quantity = 0;
            this.status = Status.OUT_OF_STOCK;
        } else {
            this.status = Status.AVAILABLE;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) {
        this.quantity = (quantity != null) ? quantity : 0;
        if (this.quantity < 1) {
            this.status = Status.OUT_OF_STOCK;
        } else {
            this.status = Status.AVAILABLE;
        }
    }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
