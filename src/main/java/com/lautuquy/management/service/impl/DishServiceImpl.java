package com.lautuquy.management.service.impl;

import com.lautuquy.management.dto.request.DishRequest;
import com.lautuquy.management.entity.Category;
import com.lautuquy.management.entity.Dish;
import com.lautuquy.management.exception.ResourceNotFoundException;
import com.lautuquy.management.repository.CategoryRepository;
import com.lautuquy.management.repository.DishRepository;
import com.lautuquy.management.service.DishService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;

    public DishServiceImpl(DishRepository dishRepository, CategoryRepository categoryRepository) {
        this.dishRepository = dishRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Dish> getAllDishes() {
        return dishRepository.findAll();
    }

    @Override
    public Page<Dish> getDishesPaged(Long categoryId, Pageable pageable) {
        if (categoryId != null && categoryId > 0) {
            return dishRepository.findByCategoryId(categoryId, pageable);
        }
        return dishRepository.findAll(pageable);
    }

    @Override
    public List<Dish> getDishesByCategoryId(Long categoryId) {
        if (categoryId != null && categoryId > 0) {
            return dishRepository.findByCategoryId(categoryId);
        }
        return dishRepository.findAll();
    }

    @Override
    public Dish getDishById(Long id) {
        return dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn", id));
    }

    @Override
    @Transactional
    public Dish createDish(DishRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", request.getCategoryId()));

        Dish dish = new Dish();
        dish.setCategory(category);
        dish.setName(request.getName());
        dish.setImageUrl(request.getImageUrl());
        dish.setPrice(request.getPrice());
        dish.setDescription(request.getDescription());
        dish.setStatus(request.getStatus() != null ? request.getStatus() : Dish.Status.AVAILABLE);

        return dishRepository.save(dish);
    }

    @Override
    @Transactional
    public Dish updateDish(Long id, DishRequest request) {
        Dish dish = getDishById(id);
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", request.getCategoryId()));

        dish.setCategory(category);
        dish.setName(request.getName());
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            dish.setImageUrl(request.getImageUrl());
        }
        dish.setPrice(request.getPrice());
        dish.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            dish.setStatus(request.getStatus());
        }

        return dishRepository.save(dish);
    }

    @Override
    @Transactional
    public void deleteDish(Long id) {
        Dish dish = getDishById(id);
        dishRepository.delete(dish);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Dish.Status status) {
        Dish dish = getDishById(id);
        dish.setStatus(status);
        dishRepository.save(dish);
    }
}
