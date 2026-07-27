package com.lautuquy.management.dto.request;

import com.lautuquy.management.entity.RestaurantTable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TableRequest {

    private Long id;

    @NotBlank(message = "Số bàn không được để trống")
    @Size(max = 10, message = "Số bàn tối đa 10 ký tự")
    private String tableNumber;

    @NotNull(message = "Loại bàn không được để trống")
    private Long tableTypeId;

    @NotNull(message = "Trạng thái không được để trống")
    private RestaurantTable.Status status = RestaurantTable.Status.EMPTY;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    public Long getTableTypeId() { return tableTypeId; }
    public void setTableTypeId(Long tableTypeId) { this.tableTypeId = tableTypeId; }

    public RestaurantTable.Status getStatus() { return status; }
    public void setStatus(RestaurantTable.Status status) { this.status = status; }
}
