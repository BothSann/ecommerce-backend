package com.ecommerce.app.model;

public enum OrderStatus {
    PENDING("PENDING", "Order is pending confirmation"),
    CONFIRMED("CONFIRMED", "Order has been confirmed"),
    SHIPPED("SHIPPED", "Order has been shipped"),
    DELIVERED("DELIVERED", "Order has been delivered");

    private final String status;
    private final String description;

    OrderStatus(String status, String description) {
        this.status = status;
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public static OrderStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return PENDING;
        }
        
        for (OrderStatus orderStatus : OrderStatus.values()) {
            if (orderStatus.status.equalsIgnoreCase(status.trim())) {
                return orderStatus;
            }
        }
        
        throw new IllegalArgumentException("Invalid order status: " + status);
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        switch (this) {
            case PENDING:
                return newStatus == CONFIRMED;
            case CONFIRMED:
                return newStatus == SHIPPED;
            case SHIPPED:
                return newStatus == DELIVERED;
            case DELIVERED:
                return false; // Final status
            default:
                return false;
        }
    }
}