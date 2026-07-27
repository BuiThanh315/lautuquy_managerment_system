package com.lautuquy.management.dto.request;

import com.lautuquy.management.entity.TableType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class TableTypeRequest {

    private Long id;

    @NotNull(message = "Sức chứa bàn không được để trống")
    @Min(value = 1, message = "Sức chứa phải tối thiểu 1 người")
    private Integer capacity;

    @NotNull(message = "Hạng bàn không được để trống")
    private TableType.TableClass tableClass = TableType.TableClass.REGULAR;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public TableType.TableClass getTableClass() { return tableClass; }
    public void setTableClass(TableType.TableClass tableClass) { this.tableClass = tableClass; }
}
