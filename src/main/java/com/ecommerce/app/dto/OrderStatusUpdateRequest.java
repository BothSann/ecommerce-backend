package com.ecommerce.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {
    private String status;
    
    public String getStatus() {
        return status != null ? status.trim().toUpperCase() : null;
    }
}