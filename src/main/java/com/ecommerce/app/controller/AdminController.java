package com.ecommerce.app.controller;

import com.ecommerce.app.dto.ProductRequest;
import com.ecommerce.app.dto.OrderStatusUpdateRequest;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.model.Order;
import com.ecommerce.app.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private FirebaseService firebaseService;

    @PostMapping("/products")
    public CompletableFuture<ResponseEntity<Product>> createProduct(@RequestBody ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setImageUrl(request.getImageUrl());

        return firebaseService.saveProduct(product)
                .thenApply(savedProduct -> {
                    if (savedProduct != null) {
                        return ResponseEntity.ok(savedProduct);
                    } else {
                        return ResponseEntity.internalServerError().<Product>build();
                    }
                })
                .exceptionally(throwable -> ResponseEntity.internalServerError().<Product>build());
    }

    @GetMapping("/products")
    public CompletableFuture<ResponseEntity<List<Product>>> getAllProducts() {
        return firebaseService.getAllProducts()
                .thenApply(products -> ResponseEntity.ok(products))
                .exceptionally(throwable -> ResponseEntity.internalServerError().build());
    }

    @GetMapping("/products/{id}")
    public CompletableFuture<ResponseEntity<Product>> getProduct(@PathVariable String id) {
        return firebaseService.getProductById(id)
                .thenApply(product -> {
                    if (product != null) {
                        return ResponseEntity.ok(product);
                    }
                    return ResponseEntity.notFound().<Product>build();
                })
                .exceptionally(throwable -> ResponseEntity.internalServerError().<Product>build());
    }

    @PutMapping("/products/{id}")
    public CompletableFuture<ResponseEntity<Product>> updateProduct(@PathVariable String id, @RequestBody ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setImageUrl(request.getImageUrl());

        return firebaseService.updateProduct(id, product)
                .thenApply(updatedProduct -> {
                    if (updatedProduct != null) {
                        return ResponseEntity.ok(updatedProduct);
                    } else {
                        return ResponseEntity.internalServerError().<Product>build();
                    }
                })
                .exceptionally(throwable -> ResponseEntity.internalServerError().<Product>build());
    }

    @DeleteMapping("/products/{id}")
    public CompletableFuture<ResponseEntity<Object>> deleteProduct(@PathVariable String id) {
        return firebaseService.deleteProduct(id)
                .thenApply(aVoid -> ResponseEntity.ok().build())
                .exceptionally(throwable -> ResponseEntity.internalServerError().body("Failed to delete product"));
    }

    @GetMapping("/users")
    public CompletableFuture<ResponseEntity<List<User>>> getAllUsers() {
        return firebaseService.getAllUsers()
                .thenApply(users -> ResponseEntity.ok(users))
                .exceptionally(throwable -> ResponseEntity.internalServerError().build());
    }

    @PutMapping("/users/{userId}/role")
    public CompletableFuture<ResponseEntity<User>> updateUserRole(@PathVariable String userId, @RequestParam String role) {
        if (!role.equals("ADMIN") && !role.equals("CUSTOMER")) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().<User>build());
        }

        return firebaseService.updateUserRole(userId, role)
                .thenApply(updatedUser -> {
                    if (updatedUser != null) {
                        return ResponseEntity.ok(updatedUser);
                    } else {
                        return ResponseEntity.internalServerError().<User>build();
                    }
                })
                .exceptionally(throwable -> ResponseEntity.internalServerError().<User>build());
    }

    @DeleteMapping("/users/{userId}")
    public CompletableFuture<ResponseEntity<Object>> deleteUser(@PathVariable String userId) {
        return firebaseService.deleteUser(userId)
                .thenApply(aVoid -> ResponseEntity.ok((Object) "User deleted successfully"))
                .exceptionally(throwable -> ResponseEntity.internalServerError().body("Failed to delete user"));
    }

    // Order management endpoints
    @GetMapping("/orders")
    public CompletableFuture<ResponseEntity<List<Order>>> getAllOrders() {
        return firebaseService.getAllOrders()
                .thenApply(orders -> {
                    if (orders != null) {
                        return ResponseEntity.ok(orders);
                    } else {
                        return ResponseEntity.internalServerError().<List<Order>>build();
                    }
                })
                .exceptionally(throwable -> ResponseEntity.internalServerError().<List<Order>>build());
    }

    @GetMapping("/orders/{orderId}")
    public CompletableFuture<ResponseEntity<Order>> getOrder(@PathVariable String orderId) {
        return firebaseService.getOrderById(orderId)
                .thenApply(order -> {
                    if (order != null) {
                        return ResponseEntity.ok(order);
                    } else {
                        return ResponseEntity.notFound().<Order>build();
                    }
                })
                .exceptionally(throwable -> ResponseEntity.internalServerError().<Order>build());
    }

    @PutMapping("/orders/{orderId}/status")
    public CompletableFuture<ResponseEntity<Order>> updateOrderStatus(
            @PathVariable String orderId, 
            @RequestBody OrderStatusUpdateRequest request) {
        
        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().<Order>build());
        }

        return firebaseService.updateOrderStatus(orderId, request.getStatus())
                .thenApply(updatedOrder -> {
                    if (updatedOrder != null) {
                        return ResponseEntity.ok(updatedOrder);
                    } else {
                        return ResponseEntity.internalServerError().<Order>build();
                    }
                })
                .exceptionally(throwable -> {
                    if (throwable.getCause() instanceof IllegalArgumentException) {
                        // Invalid status transition
                        return ResponseEntity.badRequest().<Order>build();
                    } else if (throwable.getMessage().contains("not found")) {
                        return ResponseEntity.notFound().<Order>build();
                    } else {
                        return ResponseEntity.internalServerError().<Order>build();
                    }
                });
    }
}
