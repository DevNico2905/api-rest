package com.devnico.api_rest.service;

import com.devnico.api_rest.entity.Category;
import com.devnico.api_rest.entity.Product;
import com.devnico.api_rest.entity.enums.Status;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    Product saveProduct(Product product);

    List<Product> allProducts();

    Optional<Product> findByName(String name);

    Optional<Product> findById(Long idProduct);

    Product updateProduct(Long id, Product product);

    void deleteProduct(Long id);

    Product changeStatusProduct(Long id, Status newStatus);

    List<Product> findByStatus(Status status);

    List<Product> findProductsByCategory(Category category);

    String countAll();

    String countByCategory(Category category);

    String countByStatus(Status status);

}
