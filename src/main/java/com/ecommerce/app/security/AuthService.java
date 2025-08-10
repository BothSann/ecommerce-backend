package com.ecommerce.app.security;

import com.ecommerce.app.model.User;
import com.ecommerce.app.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private FirebaseService firebaseService;


    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            User user = firebaseService.getUserByEmail(email).get();
            if (user == null) {
                throw new UsernameNotFoundException("User not found with email: " + email);
            }

            // Ensure role has proper prefix for Spring Security
            String role = user.getRole();
            if (role == null || role.trim().isEmpty()) {
                role = "CUSTOMER"; // Default fallback
            }
            
            // Add ROLE_ prefix if not present
            String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority(authority)))
                    .build();
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found with email: " + email, e);
        }
    }

    public User getUserByEmail(String email) throws Exception {
        return firebaseService.getUserByEmail(email).get();
    }


    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }

    public User authenticateUser(String email, String password) throws Exception {
        User user = firebaseService.getUserByEmail(email).get();
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!verifyPassword(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return user;
    }

    public User registerUser(String username, String email, String password) throws Exception {
        User existingUser = firebaseService.getUserByEmail(email).get();
        if (existingUser != null) {
            throw new RuntimeException("User already exists with this email");
        }

        String hashedPassword = hashPassword(password);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setRole("CUSTOMER"); // Default role

        return firebaseService.saveUser(user).get();
    }

    public User registerAdmin(String username, String email, String password) throws Exception {
        User existingUser = firebaseService.getUserByEmail(email).get();
        if (existingUser != null) {
            throw new RuntimeException("User already exists with this email");
        }

        String hashedPassword = hashPassword(password);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setRole("ADMIN"); // Admin role

        return firebaseService.saveUser(user).get();
    }
}
