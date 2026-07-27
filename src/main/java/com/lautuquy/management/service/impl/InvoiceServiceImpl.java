package com.lautuquy.management.service.impl;

import com.lautuquy.management.entity.*;
import com.lautuquy.management.exception.ResourceNotFoundException;
import com.lautuquy.management.repository.*;
import com.lautuquy.management.service.BookingService;
import com.lautuquy.management.service.InvoiceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final BookingRepository bookingRepository;
    private final RestaurantTableRepository tableRepository;
    private final BookingService bookingService;
    private final OrderItemRepository orderItemRepository;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                              OrderRepository orderRepository,
                              BookingRepository bookingRepository,
                              RestaurantTableRepository tableRepository,
                              BookingService bookingService,
                              OrderItemRepository orderItemRepository) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
        this.bookingRepository = bookingRepository;
        this.tableRepository = tableRepository;
        this.bookingService = bookingService;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Invoice requestPaymentByCustomer(String username, Invoice.PaymentMethod paymentMethod) {
        Booking seatedBooking = bookingService.getActiveSeatedBooking(username);
        if (seatedBooking == null) {
            throw new IllegalStateException("Bạn chưa được xếp bàn hoặc chưa có đơn gọi món tại bàn.");
        }

        Order activeOrder = orderRepository.findFirstByBookingIdAndStatus(seatedBooking.getId(), Order.Status.PROCESSING)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn gọi món tại bàn", seatedBooking.getId()));

        List<OrderItem> items = orderItemRepository.findByOrderId(activeOrder.getId());
        BigDecimal totalAmount = items.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        Optional<Invoice> existingInvoiceOpt = invoiceRepository.findByOrderId(activeOrder.getId());
        Invoice invoice;
        if (existingInvoiceOpt.isPresent()) {
            invoice = existingInvoiceOpt.get();
            invoice.setTotalAmount(totalAmount);
            invoice.setFinalAmount(totalAmount);
            invoice.setPaymentMethod(paymentMethod != null ? paymentMethod : Invoice.PaymentMethod.CASH);
            invoice.setPaymentStatus(Invoice.PaymentStatus.UNPAID);
        } else {
            invoice = new Invoice(
                    activeOrder,
                    totalAmount,
                    totalAmount,
                    paymentMethod != null ? paymentMethod : Invoice.PaymentMethod.CASH,
                    Invoice.PaymentStatus.UNPAID
            );
        }

        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Invoice confirmPaymentByStaff(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn đặt bàn", bookingId));

        Order order = orderRepository.findFirstByBookingIdAndStatus(bookingId, Order.Status.PROCESSING)
                .orElseGet(() -> orderRepository.findByBookingId(bookingId).stream().findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Đơn gọi món của booking", bookingId)));

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        BigDecimal totalAmount = items.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        Optional<Invoice> existingInvoiceOpt = invoiceRepository.findByOrderId(order.getId());
        Invoice invoice;
        if (existingInvoiceOpt.isPresent()) {
            invoice = existingInvoiceOpt.get();
            invoice.setTotalAmount(totalAmount);
            invoice.setFinalAmount(totalAmount);
            invoice.setPaymentStatus(Invoice.PaymentStatus.PAID);
        } else {
            invoice = new Invoice(
                    order,
                    totalAmount,
                    totalAmount,
                    Invoice.PaymentMethod.CASH,
                    Invoice.PaymentStatus.PAID
            );
        }
        invoice = invoiceRepository.save(invoice);

        // 1. Cập nhật Order status -> COMPLETED
        order.setStatus(Order.Status.COMPLETED);
        orderRepository.save(order);

        // 2. Cập nhật Booking status -> COMPLETED
        booking.setStatus(Booking.Status.COMPLETED);
        bookingRepository.save(booking);

        // 3. Cập nhật Bàn ăn -> DIRTY
        RestaurantTable table = booking.getAssignedTable();
        if (table == null) {
            table = order.getTable();
        }
        if (table != null) {
            table.setStatus(RestaurantTable.Status.DIRTY);
            tableRepository.save(table);
        }

        return invoice;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Invoice processPaymentDirect(Long orderId, Invoice.PaymentMethod paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn gọi món", orderId));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        BigDecimal totalAmount = items.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        Invoice invoice = invoiceRepository.findByOrderId(orderId).orElseGet(() -> new Invoice(
                order, totalAmount, totalAmount, paymentMethod, Invoice.PaymentStatus.PAID
        ));
        invoice.setPaymentStatus(Invoice.PaymentStatus.PAID);
        if (paymentMethod != null) {
            invoice.setPaymentMethod(paymentMethod);
        }
        invoice = invoiceRepository.save(invoice);

        order.setStatus(Order.Status.COMPLETED);
        orderRepository.save(order);

        if (order.getBooking() != null) {
            Booking booking = order.getBooking();
            booking.setStatus(Booking.Status.COMPLETED);
            bookingRepository.save(booking);

            if (booking.getAssignedTable() != null) {
                RestaurantTable table = booking.getAssignedTable();
                table.setStatus(RestaurantTable.Status.DIRTY);
                tableRepository.save(table);
            }
        } else if (order.getTable() != null) {
            RestaurantTable table = order.getTable();
            table.setStatus(RestaurantTable.Status.DIRTY);
            tableRepository.save(table);
        }

        return invoice;
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice getInvoiceByBookingId(Long bookingId) {
        return invoiceRepository.findByOrderBookingId(bookingId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAllByOrderByCreatedAtDesc();
    }
}
