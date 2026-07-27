package com.lautuquy.management.dto.request;

import com.lautuquy.management.entity.Dish;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class DishRequest {

    private Long id;

    @NotNull(message = "Danh mục món ăn không được để trống")
    private Long categoryId;

    @NotBlank(message = "Tên món ăn không được để trống")
    @Size(max = 150, message = "Tên món ăn tối đa 150 ký tự")
    private String name;

    private String imageUrl;

    @NotNull(message = "Giá món ăn không được để trống")
    @Min(value = 0, message = "Giá món ăn phải lớn hơn hoặc bằng 0")
    private BigDecimal price;

    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;

    @NotNull(message = "Số lượng món không được để trống")
    @Min(value = 0, message = "Số lượng món phải lớn hơn hoặc bằng 0")
    private Integer quantity = 50;

    private Dish.Status status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Dish.Status getStatus() { return status; }
    public void setStatus(Dish.Status status) { this.status = status; }
}
