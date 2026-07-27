package com.lautuquy.management.entity;

import jakarta.persistence.*;

/**
 * Entity mapping bảng `restaurant_tables`.
 */
@Entity
@Table(name = "restaurant_tables")
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_number", nullable = false, unique = true, length = 10)
    private String tableNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "table_type_id", nullable = false)
    private TableType tableType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Status status = Status.EMPTY;

    public enum Status {
        EMPTY, RESERVED, SERVING, DIRTY
    }

    public RestaurantTable() {}

    public RestaurantTable(Long id, String tableNumber, TableType tableType, Status status) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.tableType = tableType;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    public TableType getTableType() { return tableType; }
    public void setTableType(TableType tableType) { this.tableType = tableType; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
