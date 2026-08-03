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
    private final com.lautuquy.management.service.OrderService orderService;
    private final BookingPreorderRepository bookingPreorderRepository;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                              OrderRepository orderRepository,
                              BookingRepository bookingRepository,
                              RestaurantTableRepository tableRepository,
                              BookingService bookingService,
                              OrderItemRepository orderItemRepository,
                              com.lautuquy.management.service.OrderService orderService,
                              BookingPreorderRepository bookingPreorderRepository) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
        this.bookingRepository = bookingRepository;
        this.tableRepository = tableRepository;
        this.bookingService = bookingService;
        this.orderItemRepository = orderItemRepository;
        this.orderService = orderService;
        this.bookingPreorderRepository = bookingPreorderRepository;
    }

    private BigDecimal calculateDepositAmount(Order order) {
        if (order == null || order.getBooking() == null) {
            return BigDecimal.ZERO;
        }
        Long bookingId = order.getBooking().getId();
        List<BookingPreorder> preorders = bookingPreorderRepository.findByBookingId(bookingId);
        if (preorders == null || preorders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal preorderTotal = BigDecimal.ZERO;
        for (BookingPreorder po : preorders) {
            Dish dish = po.getDish();
            BigDecimal price = (dish != null && dish.getPrice() != null) ? dish.getPrice() : BigDecimal.ZERO;
            int qty = po.getQuantity() != null ? po.getQuantity() : 0;
            preorderTotal = preorderTotal.add(price.multiply(BigDecimal.valueOf(qty)));
        }
        return preorderTotal.multiply(new BigDecimal("0.50"));
    }

    private void ensureDepositAndFinalAmount(Invoice invoice) {
        if (invoice == null || invoice.getOrder() == null) return;
        BigDecimal deposit = calculateDepositAmount(invoice.getOrder());
        BigDecimal total = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
        if (deposit.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setDepositAmount(deposit);
            BigDecimal net = total.subtract(deposit);
            if (net.compareTo(BigDecimal.ZERO) < 0) net = BigDecimal.ZERO;
            invoice.setFinalAmount(net);
        } else if (invoice.getDepositAmount() == null) {
            invoice.setDepositAmount(BigDecimal.ZERO);
        }
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
        BigDecimal depositAmount = calculateDepositAmount(activeOrder);
        BigDecimal netAmount = totalAmount.subtract(depositAmount);
        BigDecimal finalAmount = netAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : netAmount;

        Optional<Invoice> existingInvoiceOpt = invoiceRepository.findByOrderId(activeOrder.getId());
        Invoice invoice;
        if (existingInvoiceOpt.isPresent()) {
            invoice = existingInvoiceOpt.get();
            invoice.setTotalAmount(totalAmount);
            invoice.setDepositAmount(depositAmount);
            invoice.setFinalAmount(finalAmount);
            invoice.setPaymentMethod(paymentMethod != null ? paymentMethod : Invoice.PaymentMethod.CASH);
            invoice.setPaymentStatus(Invoice.PaymentStatus.UNPAID);
        } else {
            invoice = new Invoice(
                    activeOrder,
                    totalAmount,
                    depositAmount,
                    finalAmount,
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
                        .orElseGet(() -> orderService.getOrCreateActiveOrderForBooking(bookingId)));

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        BigDecimal totalAmount = items.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal depositAmount = calculateDepositAmount(order);
        BigDecimal netAmount = totalAmount.subtract(depositAmount);
        BigDecimal finalAmount = netAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : netAmount;

        Optional<Invoice> existingInvoiceOpt = invoiceRepository.findByOrderId(order.getId());
        Invoice invoice;
        if (existingInvoiceOpt.isPresent()) {
            invoice = existingInvoiceOpt.get();
            invoice.setTotalAmount(totalAmount);
            invoice.setDepositAmount(depositAmount);
            invoice.setFinalAmount(finalAmount);
            invoice.setPaymentStatus(Invoice.PaymentStatus.PAID);
        } else {
            invoice = new Invoice(
                    order,
                    totalAmount,
                    depositAmount,
                    finalAmount,
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
        BigDecimal depositAmount = calculateDepositAmount(order);
        BigDecimal netAmount = totalAmount.subtract(depositAmount);
        BigDecimal finalAmount = netAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : netAmount;
        Invoice.PaymentMethod method = paymentMethod != null ? paymentMethod : Invoice.PaymentMethod.CASH;

        Invoice invoice = invoiceRepository.findByOrderId(orderId).orElseGet(() -> new Invoice(
                order, totalAmount, depositAmount, finalAmount, method, Invoice.PaymentStatus.PAID
        ));
        invoice.setTotalAmount(totalAmount);
        invoice.setDepositAmount(depositAmount);
        invoice.setFinalAmount(finalAmount);
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
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn", id));
        ensureDepositAndFinalAmount(invoice);
        return invoice;
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice getInvoiceByBookingId(Long bookingId) {
        Invoice invoice = invoiceRepository.findByOrderBookingId(bookingId).orElse(null);
        ensureDepositAndFinalAmount(invoice);
        return invoice;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> getAllInvoices() {
        List<Invoice> invoices = invoiceRepository.findAllByOrderByCreatedAtDesc();
        invoices.forEach(this::ensureDepositAndFinalAmount);
        return invoices;
    }
}
