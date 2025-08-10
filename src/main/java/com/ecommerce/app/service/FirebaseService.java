package com.ecommerce.app.service;

import com.ecommerce.app.config.FirebaseConfig;
import com.ecommerce.app.model.User;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.Order;
import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class FirebaseService {

    @Autowired
    private FirebaseConfig firebaseConfig;

    private DatabaseReference databaseReference;

    @PostConstruct
    public void init() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
    }


    // User operations
    public CompletableFuture<User> saveUser(User user) {
        CompletableFuture<User> future = new CompletableFuture<>();
        DatabaseReference userRef = databaseReference.child("users").push();
        user.setId(userRef.getKey());

        userRef.setValue(user, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                future.completeExceptionally(databaseError.toException());
            } else {
                future.complete(user);
            }
        });

        return future;
    }

    public CompletableFuture<User> getUserByEmail(String email) {
        CompletableFuture<User> future = new CompletableFuture<>();

        databaseReference.child("users")
                .orderByChild("email")
                .equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                User user = snapshot.getValue(User.class);
                                user.setId(snapshot.getKey());
                                future.complete(user);
                                return;
                            }
                        }
                        future.complete(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        future.completeExceptionally(databaseError.toException());
                    }
                });

        return future;
    }

    public CompletableFuture<User> getUserById(String userId) {
        CompletableFuture<User> future = new CompletableFuture<>();

        databaseReference.child("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);
                            user.setId(dataSnapshot.getKey());
                            future.complete(user);
                        } else {
                            future.complete(null);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        future.completeExceptionally(databaseError.toException());
                    }
                });

        return future;
    }

    // Product operations
    public CompletableFuture<Product> saveProduct(Product product) {
        CompletableFuture<Product> future = new CompletableFuture<>();
        DatabaseReference productRef = databaseReference.child("products").push();
        product.setId(productRef.getKey());

        productRef.setValue(product, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                future.completeExceptionally(databaseError.toException());
            } else {
                future.complete(product);
            }
        });

        return future;
    }

    public CompletableFuture<List<Product>> getAllProducts() {
        CompletableFuture<List<Product>> future = new CompletableFuture<>();

        databaseReference.child("products")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Product> products = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Product product = snapshot.getValue(Product.class);
                            product.setId(snapshot.getKey());
                            products.add(product);
                        }
                        future.complete(products);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        future.completeExceptionally(databaseError.toException());
                    }
                });

        return future;
    }

    public CompletableFuture<Product> getProductById(String id) {
        CompletableFuture<Product> future = new CompletableFuture<>();

        databaseReference.child("products").child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Product product = dataSnapshot.getValue(Product.class);
                            product.setId(dataSnapshot.getKey());
                            future.complete(product);
                        } else {
                            future.complete(null);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        future.completeExceptionally(databaseError.toException());
                    }
                });

        return future;
    }

    public CompletableFuture<Product> updateProduct(String id, Product product) {
        CompletableFuture<Product> future = new CompletableFuture<>();
        product.setId(id);

        databaseReference.child("products").child(id)
                .setValue(product, (databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        future.completeExceptionally(databaseError.toException());
                    } else {
                        future.complete(product);
                    }
                });

        return future;
    }

    public CompletableFuture<Void> deleteProduct(String id) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        databaseReference.child("products").child(id)
                .removeValue((databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        future.completeExceptionally(databaseError.toException());
                    } else {
                        future.complete(null);
                    }
                });

        return future;
    }

    // User management operations
    public CompletableFuture<List<User>> getAllUsers() {
        CompletableFuture<List<User>> future = new CompletableFuture<>();

        databaseReference.child("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<User> users = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            user.setId(snapshot.getKey());
                            users.add(user);
                        }
                        future.complete(users);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        future.completeExceptionally(databaseError.toException());
                    }
                });

        return future;
    }

    public CompletableFuture<User> updateUserRole(String userId, String role) {
        CompletableFuture<User> future = new CompletableFuture<>();

        databaseReference.child("users").child(userId).child("role")
                .setValue(role, (databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        future.completeExceptionally(databaseError.toException());
                    } else {
                        // Fetch the updated user after role update
                        getUserById(userId).thenAccept(user -> {
                            if (user != null) {
                                future.complete(user);
                            } else {
                                future.completeExceptionally(new RuntimeException("User not found after role update"));
                            }
                        }).exceptionally(throwable -> {
                            future.completeExceptionally(throwable);
                            return null;
                        });
                    }
                });

        return future;
    }

    public CompletableFuture<Void> deleteUser(String userId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        databaseReference.child("users").child(userId)
                .removeValue((databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        future.completeExceptionally(databaseError.toException());
                    } else {
                        future.complete(null);
                    }
                });

        return future;
    }

    // Order operations
    public CompletableFuture<Order> saveOrder(Order order) {
        CompletableFuture<Order> future = new CompletableFuture<>();
        DatabaseReference orderRef = databaseReference.child("orders").push();
        order.setId(orderRef.getKey());

        orderRef.setValue(order, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                future.completeExceptionally(databaseError.toException());
            } else {
                future.complete(order);
            }
        });

        return future;
    }

    public CompletableFuture<List<Order>> getOrdersByUserId(String userId) {
        CompletableFuture<List<Order>> future = new CompletableFuture<>();

        databaseReference.child("orders")
                .orderByChild("userId")
                .equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Order> orders = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Order order = snapshot.getValue(Order.class);
                            order.setId(snapshot.getKey());
                            orders.add(order);
                        }
                        // Sort by timestamp descending (newest first)
                        orders.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                        future.complete(orders);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        future.completeExceptionally(databaseError.toException());
                    }
                });

        return future;
    }

    public CompletableFuture<List<Order>> getAllOrders() {
        CompletableFuture<List<Order>> future = new CompletableFuture<>();

        databaseReference.child("orders")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Order> orders = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Order order = snapshot.getValue(Order.class);
                            order.setId(snapshot.getKey());
                            orders.add(order);
                        }
                        // Sort by timestamp descending (newest first)
                        orders.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                        future.complete(orders);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        future.completeExceptionally(databaseError.toException());
                    }
                });

        return future;
    }

    public CompletableFuture<Order> getOrderById(String orderId) {
        CompletableFuture<Order> future = new CompletableFuture<>();

        databaseReference.child("orders").child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Order order = dataSnapshot.getValue(Order.class);
                            order.setId(dataSnapshot.getKey());
                            future.complete(order);
                        } else {
                            future.complete(null);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        future.completeExceptionally(databaseError.toException());
                    }
                });

        return future;
    }

    public CompletableFuture<Order> updateOrderStatus(String orderId, String newStatus) {
        CompletableFuture<Order> future = new CompletableFuture<>();

        // First get the current order to validate the status transition
        getOrderById(orderId).thenAccept(order -> {
            if (order == null) {
                future.completeExceptionally(new RuntimeException("Order not found"));
                return;
            }

            try {
                // Parse and validate the new status
                com.ecommerce.app.model.OrderStatus orderStatus = com.ecommerce.app.model.OrderStatus.fromString(newStatus);
                
                // Update the order status with validation
                order.updateStatus(orderStatus);

                // Save the updated order back to Firebase
                databaseReference.child("orders").child(orderId)
                        .setValue(order, (databaseError, databaseReference) -> {
                            if (databaseError != null) {
                                future.completeExceptionally(databaseError.toException());
                            } else {
                                future.complete(order);
                            }
                        });

            } catch (IllegalArgumentException e) {
                future.completeExceptionally(e);
            }
        }).exceptionally(throwable -> {
            future.completeExceptionally(throwable);
            return null;
        });

        return future;
    }
}