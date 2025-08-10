package com.ecommerce.app.controller;

import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.FirebaseService;
import com.ecommerce.app.security.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private FirebaseService firebaseService;
    
    @Autowired
    private AuthService authService;

    @PostMapping
    public CompletableFuture<ResponseEntity<Order>> createOrder(@RequestBody Order order) {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getName().equals("anonymousUser")) {
                logger.error("Order creation failed: User not authenticated");
                return CompletableFuture.completedFuture(ResponseEntity.status(401).build());
            }
            
            String currentUserEmail = authentication.getName();
            logger.info("Creating order for authenticated user: {}", currentUserEmail);
            
            // Validate the order has required fields
            if (order.getUserId() == null || order.getUserId().trim().isEmpty()) {
                logger.error("Order creation failed: Missing userId");
                return CompletableFuture.completedFuture(ResponseEntity.badRequest().build());
            }
            
            if (order.getItems() == null || order.getItems().isEmpty()) {
                logger.error("Order creation failed: No items in order");
                return CompletableFuture.completedFuture(ResponseEntity.badRequest().build());
            }
            
            if (order.getTotalAmount() <= 0) {
                logger.error("Order creation failed: Invalid total amount: {}", order.getTotalAmount());
                return CompletableFuture.completedFuture(ResponseEntity.badRequest().build());
            }
            
            // Set order metadata
            order.setTimestamp(System.currentTimeMillis());
            if (order.getStatus() == null) {
                order.setStatus(com.ecommerce.app.model.OrderStatus.PENDING);
            }
            // Set timestamps if not already set
            if (order.getCreatedAt() == null) {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                order.setCreatedAt(now.format(formatter));
                order.setUpdatedAt(now.format(formatter));
            }
            
            logger.info("Processing order creation - userId: {}, itemCount: {}, total: {}", 
                order.getUserId(), order.getItems().size(), order.getTotalAmount());

            return firebaseService.saveOrder(order)
                    .thenApply(savedOrder -> {
                        if (savedOrder != null) {
                            logger.info("Order created successfully with ID: {}", savedOrder.getId());
                            return ResponseEntity.ok(savedOrder);
                        } else {
                            logger.error("Order creation failed: Firebase service returned null");
                            return ResponseEntity.internalServerError().<Order>build();
                        }
                    })
                    .exceptionally(throwable -> {
                        logger.error("Order creation failed with exception: ", throwable);
                        return ResponseEntity.internalServerError().<Order>build();
                    });
        } catch (Exception e) {
            logger.error("Order creation failed with unexpected error: ", e);
            return CompletableFuture.completedFuture(ResponseEntity.internalServerError().<Order>build());
        }
    }

    @GetMapping("/my-orders")
    public CompletableFuture<ResponseEntity<List<Order>>> getMyOrders() {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getName().equals("anonymousUser")) {
                logger.error("Get orders failed: User not authenticated");
                return CompletableFuture.completedFuture(ResponseEntity.status(401).build());
            }
            
            String currentUserEmail = authentication.getName();
            logger.info("Fetching orders for authenticated user: {}", currentUserEmail);
            
            // Get user by email to find userId
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return authService.getUserByEmail(currentUserEmail);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenCompose(user -> {
                    if (user == null) {
                        logger.error("User not found for email: {}", currentUserEmail);
                        return CompletableFuture.completedFuture(ResponseEntity.status(404).<List<Order>>build());
                    }
                    
                    // Get orders by userId
                    return firebaseService.getOrdersByUserId(user.getId())
                        .thenApply(orders -> {
                            logger.info("Found {} orders for user: {}", orders.size(), currentUserEmail);
                            return ResponseEntity.ok(orders);
                        });
                })
                .exceptionally(throwable -> {
                    logger.error("Failed to fetch orders for user: " + currentUserEmail, throwable);
                    return ResponseEntity.internalServerError().<List<Order>>build();
                });
        } catch (Exception e) {
            logger.error("Get orders failed with unexpected error: ", e);
            return CompletableFuture.completedFuture(ResponseEntity.internalServerError().<List<Order>>build());
        }
    }
}
