package com.ecommerce.app.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                System.out.println("Firebase app already initialized");
                return;
            }

            ClassPathResource serviceAccount = new ClassPathResource("firebase-service-account.json");
            if (!serviceAccount.exists()) {
                throw new RuntimeException("Firebase service account file not found");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream()))
                    .setDatabaseUrl("https://ecommerce-app-ef02e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("Firebase app initialized successfully");
        } catch (IOException e) {
            System.err.println("Failed to initialize Firebase: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }
}
