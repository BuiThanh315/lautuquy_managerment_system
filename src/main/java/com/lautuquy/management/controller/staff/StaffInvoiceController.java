package com.lautuquy.management.controller.staff;

import com.lautuquy.management.entity.Invoice;
import com.lautuquy.management.entity.OrderItem;
import com.lautuquy.management.repository.OrderItemRepository;
import com.lautuquy.management.service.InvoiceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/staff/invoices")
public class StaffInvoiceController {

    private final InvoiceService invoiceService;
    private final OrderItemRepository orderItemRepository;

    public StaffInvoiceController(InvoiceService invoiceService, OrderItemRepository orderItemRepository) {
        this.invoiceService = invoiceService;
        this.orderItemRepository = orderItemRepository;
    }

    @GetMapping
    public String listInvoices(Model model) {
        List<Invoice> invoices = invoiceService.getAllInvoices();
        model.addAttribute("invoices", invoices);
        model.addAttribute("pageTitle", "Danh Sách Hóa Đơn — Staff");
        return "staff/invoice-list";
    }

    @GetMapping("/{id}/print")
    public String printInvoice(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(invoice.getOrder().getId());

        model.addAttribute("invoice", invoice);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("pageTitle", "In Hóa Đơn #" + invoice.getId() + " — Lẩu Tứ Quý");

        return "staff/invoice-print";
    }
}
