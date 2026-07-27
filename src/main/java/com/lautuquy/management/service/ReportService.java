package com.lautuquy.management.service;

import java.math.BigDecimal;
import java.util.Map;

public interface ReportService {

    BigDecimal getTodayRevenue();

    BigDecimal getMonthlyRevenue();

    BigDecimal getYearlyRevenue();

    long getTotalPaidInvoicesCount();

    long getTotalBookingsCount();

    long getTotalDishesCount();

    Map<String, BigDecimal> getMonthlyRevenueChartData();
}
