package com.lautuquy.management.service;

import com.lautuquy.management.dto.request.DishRequest;
import com.lautuquy.management.entity.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DishService {
    List<Dish> getAllDishes();
    Page<Dish> getDishesPaged(Long categoryId, Pageable pageable);
    List<Dish> getDishesByCategoryId(Long categoryId);
    Dish getDishById(Long id);
    Dish createDish(DishRequest request);
    Dish updateDish(Long id, DishRequest request);
    void deleteDish(Long id);
    void updateStatus(Long id, Dish.Status status);
}
