package com.lautuquy.management.controller.admin;

import com.lautuquy.management.entity.Invoice;
import com.lautuquy.management.service.InvoiceService;
import com.lautuquy.management.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping
public class AdminDashboardController {

    private final ReportService reportService;
    private final InvoiceService invoiceService;

    public AdminDashboardController(ReportService reportService, InvoiceService invoiceService) {
        this.reportService = reportService;
        this.invoiceService = invoiceService;
    }

    @GetMapping({"/admin/dashboard", "/admin"})
    public String viewDashboard(Model model) {
        BigDecimal todayRevenue = reportService.getTodayRevenue();
        BigDecimal monthlyRevenue = reportService.getMonthlyRevenue();
        BigDecimal yearlyRevenue = reportService.getYearlyRevenue();
        long paidInvoicesCount = reportService.getTotalPaidInvoicesCount();
        long bookingsCount = reportService.getTotalBookingsCount();
        long dishesCount = reportService.getTotalDishesCount();
        Map<String, BigDecimal> chartData = reportService.getMonthlyRevenueChartData();
        List<Invoice> recentInvoices = invoiceService.getAllInvoices().stream().limit(5).toList();

        model.addAttribute("todayRevenue", todayRevenue);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("yearlyRevenue", yearlyRevenue);
        model.addAttribute("paidInvoicesCount", paidInvoicesCount);
        model.addAttribute("bookingsCount", bookingsCount);
        model.addAttribute("dishesCount", dishesCount);
        model.addAttribute("chartData", chartData);
        model.addAttribute("recentInvoices", recentInvoices);
        model.addAttribute("pageTitle", "Dashboard Báo Cáo Doanh Thu — Admin");

        return "admin/dashboard";
    }
}
