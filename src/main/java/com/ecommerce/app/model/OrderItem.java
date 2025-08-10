package com.ecommerce.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class OrderItem {
    private String productId;
    private String productName;
    private int quantity;
    private double price;
}
