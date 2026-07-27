package com.lautuquy.management.service;

import com.lautuquy.management.entity.Invoice;

import java.util.List;

public interface InvoiceService {

    Invoice requestPaymentByCustomer(String username, Invoice.PaymentMethod paymentMethod);

    Invoice confirmPaymentByStaff(Long bookingId);

    Invoice processPaymentDirect(Long orderId, Invoice.PaymentMethod paymentMethod);

    Invoice getInvoiceById(Long id);

    Invoice getInvoiceByBookingId(Long bookingId);

    List<Invoice> getAllInvoices();
}
