package com.lautuquy.management.dto.response;

public class TableOptionDto {
    private Long id;
    private String tableNumber;
    private Integer capacity;
    private String tableClass;
    private boolean lockedByOther;
    private boolean lockedByMe;
    private boolean booked;
    private boolean available;

    public TableOptionDto() {}

    public TableOptionDto(Long id, String tableNumber, Integer capacity, String tableClass,
                          boolean lockedByOther, boolean lockedByMe, boolean booked, boolean available) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.tableClass = tableClass;
        this.lockedByOther = lockedByOther;
        this.lockedByMe = lockedByMe;
        this.booked = booked;
        this.available = available;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getTableClass() { return tableClass; }
    public void setTableClass(String tableClass) { this.tableClass = tableClass; }

    public boolean isLockedByOther() { return lockedByOther; }
    public void setLockedByOther(boolean lockedByOther) { this.lockedByOther = lockedByOther; }

    public boolean isLockedByMe() { return lockedByMe; }
    public void setLockedByMe(boolean lockedByMe) { this.lockedByMe = lockedByMe; }

    public boolean isBooked() { return booked; }
    public void setBooked(boolean booked) { this.booked = booked; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
