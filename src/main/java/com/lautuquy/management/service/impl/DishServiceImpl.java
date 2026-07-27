package com.lautuquy.management.service.impl;

import com.lautuquy.management.dto.request.DishRequest;
import com.lautuquy.management.entity.Category;
import com.lautuquy.management.entity.Dish;
import com.lautuquy.management.exception.ResourceNotFoundException;
import com.lautuquy.management.repository.BookingPreorderRepository;
import com.lautuquy.management.repository.CategoryRepository;
import com.lautuquy.management.repository.DishRepository;
import com.lautuquy.management.repository.OrderItemRepository;
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
    private final OrderItemRepository orderItemRepository;
    private final BookingPreorderRepository bookingPreorderRepository;

    public DishServiceImpl(DishRepository dishRepository,
                           CategoryRepository categoryRepository,
                           OrderItemRepository orderItemRepository,
                           BookingPreorderRepository bookingPreorderRepository) {
        this.dishRepository = dishRepository;
        this.categoryRepository = categoryRepository;
        this.orderItemRepository = orderItemRepository;
        this.bookingPreorderRepository = bookingPreorderRepository;
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
        dish.setQuantity(request.getQuantity() != null ? request.getQuantity() : 0);
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
        if (request.getQuantity() != null) {
            dish.setQuantity(request.getQuantity());
        }
        if (request.getStatus() != null) {
            dish.setStatus(request.getStatus());
        }

        return dishRepository.save(dish);
    }

    @Override
    @Transactional
    public void deleteDish(Long id) {
        Dish dish = getDishById(id);
        boolean hasOrderItems = orderItemRepository.existsByDishId(id);
        boolean hasPreorders = bookingPreorderRepository.existsByDishId(id);

        if (hasOrderItems || hasPreorders) {
            // Món đã có trong lịch sử -> Chuyển số lượng về 0 và trạng thái OUT_OF_STOCK
            dish.setQuantity(0);
            dish.setStatus(Dish.Status.OUT_OF_STOCK);
            dishRepository.save(dish);
            throw new IllegalArgumentException("Món '" + dish.getName() + "' đã từng có trong lịch sử đơn hàng/đặt bàn nên không thể xóa vĩnh viễn. Hệ thống đã tự động chuyển số lượng về 0 (Tạm hết hàng).");
        } else {
            // Chưa từng được gọi -> xóa vĩnh viễn
            dishRepository.delete(dish);
        }
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Dish.Status status) {
        Dish dish = getDishById(id);
        if (status == Dish.Status.AVAILABLE && (dish.getQuantity() == null || dish.getQuantity() < 1)) {
            dish.setQuantity(10);
        } else if (status == Dish.Status.OUT_OF_STOCK) {
            dish.setQuantity(0);
        }
        dish.setStatus(status);
        dishRepository.save(dish);
    }
}
