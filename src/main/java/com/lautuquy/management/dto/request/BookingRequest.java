package com.lautuquy.management.dto.request;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BookingRequest {

    @NotBlank(message = "Tên người đặt không được để trống")
    @Size(max = 100, message = "Tên người đặt tối đa 100 ký tự")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(\\+84|0)[0-9]{9}$", message = "Số điện thoại không hợp lệ")
    private String customerPhone;

    @NotNull(message = "Ngày đặt bàn không được để trống")
    @FutureOrPresent(message = "Ngày đặt bàn không thể trong quá khứ")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookingDate;

    @NotNull(message = "Giờ đặt bàn không được để trống")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime bookingTime;

    @NotNull(message = "Loại bàn không được để trống")
    private Long tableTypeId;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
    private String specialNotes;

    private List<PreorderItemRequest> preorders = new ArrayList<>();

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public LocalTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalTime bookingTime) { this.bookingTime = bookingTime; }

    public Long getTableTypeId() { return tableTypeId; }
    public void setTableTypeId(Long tableTypeId) { this.tableTypeId = tableTypeId; }

    public String getSpecialNotes() { return specialNotes; }
    public void setSpecialNotes(String specialNotes) { this.specialNotes = specialNotes; }

    public List<PreorderItemRequest> getPreorders() { return preorders; }
    public void setPreorders(List<PreorderItemRequest> preorders) { this.preorders = preorders; }
}
