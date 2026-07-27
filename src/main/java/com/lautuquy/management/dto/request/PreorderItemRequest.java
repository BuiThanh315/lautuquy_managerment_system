package com.lautuquy.management.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PreorderItemRequest {

    @NotNull(message = "Món ăn không được để trống")
    private Long dishId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng món đặt trước phải tối thiểu là 1")
    private Integer quantity;

    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
