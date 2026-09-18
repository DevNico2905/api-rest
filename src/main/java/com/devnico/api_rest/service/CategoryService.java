package com.devnico.api_rest.service;

import com.devnico.api_rest.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    Category createCategory(Category category);

    List<Category> getAllCategories();

    Category updateCategory(Long id, Category category);

    void deleteCategory(Long id);

    Optional<Category> findByCategoryName(String categoryName);

    Optional<Category> findById(Long id);

    boolean existsByCategoryName(String categoryName);
}
