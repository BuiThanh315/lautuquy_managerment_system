package com.lautuquy.management.service;

import com.lautuquy.management.dto.request.CategoryRequest;
import com.lautuquy.management.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(Long id);
    Category createCategory(CategoryRequest request);
    Category updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
}
