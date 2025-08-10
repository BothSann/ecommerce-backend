package com.ecommerce.app.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String id;
    private String userId;
    private List<OrderItem> items;
    private double totalAmount;
    private OrderStatus status = OrderStatus.PENDING;
    private long timestamp; // For backward compatibility
    private String createdAt;
    private String updatedAt;
    
    public Order(String userId, List<OrderItem> items, double totalAmount) {
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING;
        this.timestamp = System.currentTimeMillis();
        
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        this.createdAt = now.format(formatter);
        this.updatedAt = now.format(formatter);
    }
    
    public void updateStatus(OrderStatus newStatus) {
        if (this.status.canTransitionTo(newStatus)) {
            this.status = newStatus;
            this.updatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else {
            throw new IllegalArgumentException(
                String.format("Cannot transition from %s to %s", this.status, newStatus)
            );
        }
    }
    
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}

