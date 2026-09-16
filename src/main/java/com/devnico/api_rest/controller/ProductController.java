package com.devnico.api_rest.controller;

import com.devnico.api_rest.entity.Category;
import com.devnico.api_rest.entity.Product;
import com.devnico.api_rest.entity.enums.Status;
import com.devnico.api_rest.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    //Es mejor este forma de las ResponseEntity que el .ok(), ya que esta tiene una respuesta más concreta 201
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product newProduct){
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.saveProduct(newProduct));
    }

    //Para el GET está bien el .ok()
    @GetMapping
    public ResponseEntity<List<Product>> allProducts(){
        return ResponseEntity.ok(productService.allProducts());
    }

    @GetMapping("/find-by-name/{name}")
    public ResponseEntity<?> findByName(@PathVariable String name){
        Optional<Product> product = productService.findByName(name);
        return product.isPresent() ? ResponseEntity.ok(product.get()) :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
    }

    @GetMapping("/find-by-id/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        Optional<Product> product = productService.findById(id);
        return product.isPresent() ? ResponseEntity.ok(product.get()) :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id,
                                                 @RequestBody Product producto){
        return ResponseEntity.ok(productService.updateProduct(id, producto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Product> changeStatus(@PathVariable Long id,
                                          @RequestBody Status status){
        return ResponseEntity.ok(productService.changeStatusProduct(id,status));
    }

    @GetMapping("/list-by-status/{status}")
    public ResponseEntity<List<Product>> listByStatus(@PathVariable Status status){
        return ResponseEntity.ok(productService.findByStatus(status));
    }

    @GetMapping("/get-all-by-category/{category}")
    public ResponseEntity<List<Product>> findAllByCategory(@PathVariable Category category){
        return ResponseEntity.ok(productService.findProductsByCategory(category));
    }

    @GetMapping("/count-all")
    public ResponseEntity<String> countAllProducts(){
        return ResponseEntity.ok(productService.countAll());
    }

    @GetMapping("/count-by-category/{category}")
    public ResponseEntity<String> countByCategory(@PathVariable Category category){
        return ResponseEntity.ok(productService.countByCategory(category));
    }

    @GetMapping("/count-by-status/{status}")
    public ResponseEntity<String> countByStatus(@PathVariable Status status){
        return ResponseEntity.ok(productService.countByStatus(status));
    }
}