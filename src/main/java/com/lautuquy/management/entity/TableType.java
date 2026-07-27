package com.lautuquy.management.entity;

import jakarta.persistence.*;

/**
 * Entity mapping bảng `table_types`.
 */
@Entity
@Table(name = "table_types")
public class TableType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "class", nullable = false, length = 10)
    private TableClass tableClass = TableClass.REGULAR;

    public enum TableClass {
        REGULAR, VIP
    }

    public TableType() {}

    public TableType(Long id, Integer capacity, TableClass tableClass) {
        this.id = id;
        this.capacity = capacity;
        this.tableClass = tableClass;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public TableClass getTableClass() { return tableClass; }
    public void setTableClass(TableClass tableClass) { this.tableClass = tableClass; }
}
