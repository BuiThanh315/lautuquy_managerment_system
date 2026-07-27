package com.lautuquy.management.service.impl;

import com.lautuquy.management.entity.Invoice;
import com.lautuquy.management.repository.BookingRepository;
import com.lautuquy.management.repository.DishRepository;
import com.lautuquy.management.repository.InvoiceRepository;
import com.lautuquy.management.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;
    private final DishRepository dishRepository;

    public ReportServiceImpl(InvoiceRepository invoiceRepository,
                             BookingRepository bookingRepository,
                             DishRepository dishRepository) {
        this.invoiceRepository = invoiceRepository;
        this.bookingRepository = bookingRepository;
        this.dishRepository = dishRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTodayRevenue() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        BigDecimal sum = invoiceRepository.sumRevenueBetween(Invoice.PaymentStatus.PAID, startOfDay, endOfDay);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMonthlyRevenue() {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);
        BigDecimal sum = invoiceRepository.sumRevenueBetween(Invoice.PaymentStatus.PAID, startOfMonth, endOfMonth);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getYearlyRevenue() {
        int currentYear = LocalDate.now().getYear();
        LocalDateTime startOfYear = LocalDate.of(currentYear, 1, 1).atStartOfDay();
        LocalDateTime endOfYear = LocalDate.of(currentYear, 12, 31).atTime(LocalTime.MAX);
        BigDecimal sum = invoiceRepository.sumRevenueBetween(Invoice.PaymentStatus.PAID, startOfYear, endOfYear);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalPaidInvoicesCount() {
        return invoiceRepository.findByPaymentStatusOrderByCreatedAtDesc(Invoice.PaymentStatus.PAID).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalBookingsCount() {
        return bookingRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalDishesCount() {
        return dishRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getMonthlyRevenueChartData() {
        Map<String, BigDecimal> chartData = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.from(now.minusMonths(i));
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.atEndOfMonth().atTime(LocalTime.MAX);
            BigDecimal sum = invoiceRepository.sumRevenueBetween(Invoice.PaymentStatus.PAID, start, end);
            chartData.put("Tháng " + ym.format(formatter), sum != null ? sum : BigDecimal.ZERO);
        }

        return chartData;
    }
}
