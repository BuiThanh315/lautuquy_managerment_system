package com.lautuquy.management.controller.admin;

import com.lautuquy.management.dto.request.DishRequest;
import com.lautuquy.management.entity.Dish;
import com.lautuquy.management.service.CategoryService;
import com.lautuquy.management.service.DishService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/dishes")
public class DishAdminController {

    private final DishService dishService;
    private final CategoryService categoryService;

    public DishAdminController(DishService dishService, CategoryService categoryService) {
        this.dishService = dishService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listDishes(@RequestParam(required = false) Long categoryId, Model model) {
        model.addAttribute("dishes", dishService.getDishesByCategoryId(categoryId));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("dishRequest", new DishRequest());
        model.addAttribute("pageTitle", "Quản lý Thực đơn");
        return "admin/dishes";
    }

    @PostMapping
    public String createDish(@Valid @ModelAttribute("dishRequest") DishRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("dishes", dishService.getAllDishes());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/dishes";
        }
        try {
            dishService.createDish(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm món ăn mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dishes";
    }

    @PostMapping("/{id}/update")
    public String updateDish(@PathVariable Long id,
                             @Valid @ModelAttribute DishRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu cập nhật món ăn không hợp lệ.");
            return "redirect:/admin/dishes";
        }
        try {
            dishService.updateDish(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật món ăn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dishes";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Dish dish = dishService.getDishById(id);
            Dish.Status newStatus = (dish.getStatus() == Dish.Status.AVAILABLE) ? Dish.Status.OUT_OF_STOCK : Dish.Status.AVAILABLE;
            dishService.updateStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đổi trạng thái món thành: " + (newStatus == Dish.Status.AVAILABLE ? "Đang phục vụ" : "Tạm hết hàng"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dishes";
    }

    @PostMapping("/{id}/delete")
    public String deleteDish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            dishService.deleteDish(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa món ăn!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dishes";
    }
}
