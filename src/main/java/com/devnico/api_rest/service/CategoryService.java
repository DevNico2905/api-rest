package com.devnico.api_rest.service;

import com.devnico.api_rest.entity.Category;

import java.util.List;

public interface CategoryService {

    Category createCategory(Category category);

    List<Category> getAllCategories();
}
