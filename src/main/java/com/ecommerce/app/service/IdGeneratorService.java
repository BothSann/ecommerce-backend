package com.ecommerce.app.service;

import com.ecommerce.app.model.IdCounter;
import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;

@Service
public class IdGeneratorService {

    private DatabaseReference databaseReference;

    public enum IdType {
        USER("USR", "users"),
        PRODUCT("PRD", "products"),
        ORDER("ORD", "orders");

        private final String prefix;
        private final String counterKey;

        IdType(String prefix, String counterKey) {
            this.prefix = prefix;
            this.counterKey = counterKey;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getCounterKey() {
            return counterKey;
        }
    }

    @PostConstruct
    public void init() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Generates a thread-safe sequential ID for the specified type
     * @param type The type of entity (USER, PRODUCT, ORDER)
     * @return CompletableFuture containing the generated ID
     */
    public CompletableFuture<String> generateId(IdType type) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        String counterPath = "counters/" + type.getCounterKey();
        DatabaseReference counterRef = databaseReference.child(counterPath);
        
        // Use Firebase transaction for atomic increment - Firebase handles concurrency
        counterRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Long currentValue = mutableData.getValue(Long.class);
                if (currentValue == null) {
                    // Initialize counter if it doesn't exist
                    mutableData.setValue(1L);
                } else {
                    // Increment the counter
                    mutableData.setValue(currentValue + 1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    future.completeExceptionally(databaseError.toException());
                } else if (committed) {
                    Long newValue = dataSnapshot.getValue(Long.class);
                    if (newValue != null) {
                        String formattedId = formatId(type.getPrefix(), newValue);
                        future.complete(formattedId);
                    } else {
                        future.completeExceptionally(new RuntimeException("Failed to get counter value"));
                    }
                } else {
                    future.completeExceptionally(new RuntimeException("Transaction not committed"));
                }
            }
        });
        
        return future;
    }

    /**
     * Formats the ID with prefix and zero-padding
     * @param prefix The prefix (USR, PRD, ORD)
     * @param number The sequential number
     * @return Formatted ID (e.g., USR-0001)
     */
    private String formatId(String prefix, Long number) {
        return String.format("%s-%04d", prefix, number);
    }

    /**
     * Gets the current counter value for a specific type (for testing/debugging)
     * @param type The type of counter to retrieve
     * @return CompletableFuture containing the current counter value
     */
    public CompletableFuture<Long> getCurrentCounter(IdType type) {
        CompletableFuture<Long> future = new CompletableFuture<>();
        
        String counterPath = "counters/" + type.getCounterKey();
        DatabaseReference counterRef = databaseReference.child(counterPath);
        
        counterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long value = dataSnapshot.getValue(Long.class);
                future.complete(value != null ? value : 0L);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });
        
        return future;
    }

    /**
     * Resets a counter (for testing purposes only - use with caution in production)
     * @param type The type of counter to reset
     * @return CompletableFuture indicating completion
     */
    public CompletableFuture<Void> resetCounter(IdType type) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        String counterPath = "counters/" + type.getCounterKey();
        DatabaseReference counterRef = databaseReference.child(counterPath);
        
        counterRef.setValue(0L, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                future.completeExceptionally(databaseError.toException());
            } else {
                future.complete(null);
            }
        });
        
        return future;
    }
}