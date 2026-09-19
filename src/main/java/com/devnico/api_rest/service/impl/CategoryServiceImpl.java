package com.devnico.api_rest.service.impl;
import com.devnico.api_rest.entity.Category;
import com.devnico.api_rest.repository.CategoryRepository;
import com.devnico.api_rest.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category createCategory(Category category) {
        if (categoryRepository.existsByCategoryName(category.getCategoryName())){
            throw new RuntimeException("This category already exists.");
        }
        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category updateCategory(Long id, Category category) {
        Category categoryDb = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        categoryDb.setCategoryName(category.getCategoryName());

        return categoryRepository.save(categoryDb);
    }

    @Override
    public void deleteCategory(Long id) {
        Category categoryDb = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        categoryRepository.deleteById(id);
    }

    @Override
    public Category findByCategoryName(String categoryName) {
        return categoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public boolean existsByCategoryName(String categoryName) {
        return categoryRepository.existsByCategoryName(categoryName);
    }
}
