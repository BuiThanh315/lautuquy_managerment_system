package com.lautuquy.management.controller.customer;

import com.lautuquy.management.entity.Dish;
import com.lautuquy.management.service.CategoryService;
import com.lautuquy.management.service.DishService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/customer/menu")
public class MenuController {

    private final DishService dishService;
    private final CategoryService categoryService;

    public MenuController(DishService dishService, CategoryService categoryService) {
        this.dishService = dishService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String viewMenu(@RequestParam(required = false) Long categoryId,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "8") int size,
                           Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Dish> dishPage = dishService.getDishesPaged(categoryId, pageable);

        model.addAttribute("dishPage", dishPage);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", dishPage.getTotalPages());
        model.addAttribute("pageTitle", "Thực đơn Nhà hàng Lẩu Tứ Quý");

        return "customer/menu";
    }
}
