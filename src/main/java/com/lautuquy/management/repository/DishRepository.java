package com.lautuquy.management.repository;

import com.lautuquy.management.entity.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findByCategoryId(Long categoryId);
    Page<Dish> findByCategoryId(Long categoryId, Pageable pageable);
    List<Dish> findByStatus(Dish.Status status);
}
