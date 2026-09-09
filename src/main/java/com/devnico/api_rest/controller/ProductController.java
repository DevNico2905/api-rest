package com.devnico.api_rest.controller;

import com.devnico.api_rest.entity.Product;
import com.devnico.api_rest.service.ProductService;
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
    @PostMapping("/create")
    public ResponseEntity<Product> createProduct(@RequestBody Product newProduct){
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
}
