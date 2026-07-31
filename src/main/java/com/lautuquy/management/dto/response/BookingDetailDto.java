package com.lautuquy.management.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BookingDetailDto {

    private Long id;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String bookingDate;
    private String bookingTime;
    private String tableTypeName;
    private String tableName;
    private String specialNotes;
    private String status;
    private String statusDisplayName;
    private String createdAt;
    private boolean overdue;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal depositAmount = BigDecimal.ZERO;
    private List<PreorderItemDto> preorders = new ArrayList<>();

    public BookingDetailDto() {}

    public static class PreorderItemDto {
        private Long dishId;
        private String dishName;
        private String imageUrl;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal totalPrice;

        public PreorderItemDto() {}

        public PreorderItemDto(Long dishId, String dishName, String imageUrl, BigDecimal price, Integer quantity, BigDecimal totalPrice) {
            this.dishId = dishId;
            this.dishName = dishName;
            this.imageUrl = imageUrl;
            this.price = price;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
        }

        public Long getDishId() { return dishId; }
        public void setDishId(Long dishId) { this.dishId = dishId; }

        public String getDishName() { return dishName; }
        public void setDishName(String dishName) { this.dishName = dishName; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getBookingTime() { return bookingTime; }
    public void setBookingTime(String bookingTime) { this.bookingTime = bookingTime; }

    public String getTableTypeName() { return tableTypeName; }
    public void setTableTypeName(String tableTypeName) { this.tableTypeName = tableTypeName; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getSpecialNotes() { return specialNotes; }
    public void setSpecialNotes(String specialNotes) { this.specialNotes = specialNotes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusDisplayName() { return statusDisplayName; }
    public void setStatusDisplayName(String statusDisplayName) { this.statusDisplayName = statusDisplayName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isOverdue() { return overdue; }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }

    public List<PreorderItemDto> getPreorders() { return preorders; }
    public void setPreorders(List<PreorderItemDto> preorders) { this.preorders = preorders; }
}
