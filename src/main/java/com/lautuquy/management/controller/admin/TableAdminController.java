package com.lautuquy.management.controller.admin;

import com.lautuquy.management.dto.request.TableRequest;
import com.lautuquy.management.dto.request.TableTypeRequest;
import com.lautuquy.management.service.TableService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/tables")
public class TableAdminController {

    private final TableService tableService;

    public TableAdminController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public String listTables(Model model) {
        model.addAttribute("tables", tableService.getAllTables());
        model.addAttribute("tableTypes", tableService.getAllTableTypes());
        model.addAttribute("tableRequest", new TableRequest());
        model.addAttribute("tableTypeRequest", new TableTypeRequest());
        model.addAttribute("pageTitle", "Quản lý Bàn ăn & Loại bàn");
        return "admin/tables";
    }

    @PostMapping("/types")
    public String createTableType(@Valid @ModelAttribute("tableTypeRequest") TableTypeRequest request,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu loại bàn không hợp lệ.");
            return "redirect:/admin/tables";
        }
        try {
            tableService.createTableType(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm loại bàn mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/tables";
    }

    @PostMapping("/types/{id}/delete")
    public String deleteTableType(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tableService.deleteTableType(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa loại bàn!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa loại bàn này (có thể đang có bàn sử dụng).");
        }
        return "redirect:/admin/tables";
    }

    @PostMapping
    public String createTable(@Valid @ModelAttribute("tableRequest") TableRequest request,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu bàn không hợp lệ.");
            return "redirect:/admin/tables";
        }
        try {
            tableService.createTable(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm bàn mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/tables";
    }

    @PostMapping("/{id}/update")
    public String updateTable(@PathVariable Long id,
                              @Valid @ModelAttribute TableRequest request,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu cập nhật bàn không hợp lệ.");
            return "redirect:/admin/tables";
        }
        try {
            tableService.updateTable(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật bàn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/tables";
    }

    @PostMapping("/{id}/delete")
    public String deleteTable(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tableService.deleteTable(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa bàn!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/tables";
    }
}
