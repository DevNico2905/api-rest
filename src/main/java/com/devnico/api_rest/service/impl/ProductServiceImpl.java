package com.devnico.api_rest.service.impl;

import com.devnico.api_rest.entity.Category;
import com.devnico.api_rest.entity.Product;
import com.devnico.api_rest.entity.enums.Status;
import com.devnico.api_rest.repository.ProductRepository;
import com.devnico.api_rest.service.ProductService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> allProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> findByName(String name) {
        return productRepository.findByProductName(name);
    }

    @Override
    public Optional<Product> findById(Long idProduct) {
        return productRepository.findById(idProduct);
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        Product productFound = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found."));

        productFound.setProductName(product.getProductName());
        productFound.setDescription(product.getDescription());
        productFound.setPrice(product.getPrice());
        productFound.setAmount(product.getAmount());
        productFound.setStatus(product.getStatus());
        productFound.setCategory(product.getCategory());

        return productRepository.save(productFound);

    }

    @Override
    public void deleteProduct(Long id) {
        Product productFound = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found."));

        productRepository.deleteById(id);
    }

    @Override
    public Product changeStatusProduct(Long id, Status newStatus) {
        Product productFound = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found."));

        productFound.setStatus(newStatus);

        return productRepository.save(productFound);
    }

    @Override
    public List<Product> findByStatus(Status status) {
        return productRepository.findByStatus(status);
    }

    @Override
    public List<Product> findProductsByCategory(Category category) {
        return productRepository.findProductsByCategory(category);
    }

    @Override
    public String countAll() {
        long amount = productRepository.count();
        return "There are " + amount + " products.";
    }

    @Override
    public String countByCategory(Category category) {
        // List<Product> productsByCategory = productRepository.findAll().stream()
        //        .
        List<Product> productsByCategory = findProductsByCategory(category);
        long amount = productsByCategory.size();

        if (amount == 0 || amount > 1) return "There are " + amount + " products by '" + category.getCategoryName() + "' category.";

        return "There are " + amount + " product by '" + category.getCategoryName() + "' category.";
    }

    @Override
    public String countByStatus(Status status) {
        long amount = productRepository.countAllByStatus(status);
        String noun = amount == 1 ? "product" : "products";
        return "There are " + amount + " " + noun + " by '" + status.name() + "' status.";
    }
}
