package com.ecommerce.app.controller;

import com.ecommerce.app.dto.ProductRequest;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private FirebaseService firebaseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<Object>> createProduct(@RequestBody ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setImageUrl(request.getImageUrl());

        return firebaseService.saveProduct(product)
                .thenApply(savedProduct -> ResponseEntity.ok((Object) savedProduct))
                .exceptionally(throwable -> ResponseEntity.internalServerError().body("Failed to create product"));
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<List<Product>>> getAllProducts() {
        return firebaseService.getAllProducts()
                .thenApply(products -> ResponseEntity.ok(products))
                .exceptionally(throwable -> ResponseEntity.internalServerError().build());
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<Object>> getProduct(@PathVariable String id) {
        return firebaseService.getProductById(id)
                .thenApply(product -> {
                    if (product != null) {
                        return ResponseEntity.ok((Object) product);
                    }
                    return ResponseEntity.notFound().build();
                })
                .exceptionally(throwable -> ResponseEntity.internalServerError().body("Failed to fetch product"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<Object>> updateProduct(@PathVariable String id, @RequestBody ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setImageUrl(request.getImageUrl());

        return firebaseService.updateProduct(id, product)
                .thenApply(aVoid -> ResponseEntity.ok().build())
                .exceptionally(throwable -> ResponseEntity.internalServerError().body("Failed to update product"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<Object>> deleteProduct(@PathVariable String id) {
        return firebaseService.deleteProduct(id)
                .thenApply(aVoid -> ResponseEntity.ok().build())
                .exceptionally(throwable -> ResponseEntity.internalServerError().body("Failed to delete product"));
    }
}