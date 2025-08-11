package com.ecommerce.app.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdCounter {
    private String type; // "USER", "PRODUCT", "ORDER"
    private long counter;
    
    public IdCounter(String type) {
        this.type = type;
        this.counter = 0L;
    }
    
    public long incrementAndGet() {
        return ++this.counter;
    }
}