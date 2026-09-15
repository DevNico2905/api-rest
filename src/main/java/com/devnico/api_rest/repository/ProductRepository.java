package com.devnico.api_rest.repository;
import com.devnico.api_rest.entity.Category;
import com.devnico.api_rest.entity.Product;
import com.devnico.api_rest.entity.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductName(String productName);

    List<Product> findByStatus(Status status);

    List<Product> findProductsByCategory(Category category);

}
