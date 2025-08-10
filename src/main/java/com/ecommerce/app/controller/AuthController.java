package com.ecommerce.app.controller;

import com.ecommerce.app.dto.LoginRequest;
import com.ecommerce.app.dto.RegisterRequest;
import com.ecommerce.app.model.User;
import com.ecommerce.app.security.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            User user = authService.registerUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword()
            );
            
            logger.info("User registered successfully: {}", user.getEmail());
            
            // Automatically log in the newly registered user
            HttpSession session = httpRequest.getSession(true);
            logger.info("Created session for new user with ID: {}", session.getId());
            
            // Set up Spring Security authentication in session
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                authService.loadUserByUsername(user.getEmail());
            
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            
            // Store security context in session
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
            
            logger.info("Session established for new user: {} with role: {}", user.getEmail(), user.getRole());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user);
            response.put("message", "User registered and logged in successfully");
            response.put("sessionId", session.getId()); // For debugging only
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Registration failed for email: {}", request.getEmail(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest request) {
        try {
            User user = authService.registerAdmin(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user);
            response.put("message", "Admin user registered successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            logger.info("Login attempt for email: {}", request.getEmail());
            logger.info("Request Origin: {}", httpRequest.getHeader("Origin"));
            logger.info("User-Agent: {}", httpRequest.getHeader("User-Agent"));
            
            User user = authService.authenticateUser(
                    request.getEmail(),
                    request.getPassword()
            );

            // Create session and set authentication
            HttpSession session = httpRequest.getSession(true);
            logger.info("Created session with ID: {}", session.getId());
            
            // Set up Spring Security authentication in session
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                authService.loadUserByUsername(user.getEmail());
            
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            
            // Store security context in session
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
            
            logger.info("Session established for user: {} with role: {}", user.getEmail(), user.getRole());
            logger.info("Session cookie will be sent with name: JSESSIONID");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user);
            response.put("message", "Login successful");
            response.put("sessionId", session.getId()); // For debugging only

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login failed for email: {}", request.getEmail(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid credentials");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            logger.info("Invalidating session: {}", session.getId());
            session.invalidate();
        }
        
        SecurityContextHolder.clearContext();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logged out successfully");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/session-status")
    public ResponseEntity<?> getSessionStatus(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        
        if (session != null) {
            response.put("sessionExists", true);
            response.put("sessionId", session.getId());
            response.put("sessionMaxInactive", session.getMaxInactiveInterval());
            response.put("sessionCreationTime", session.getCreationTime());
            response.put("sessionLastAccessed", session.getLastAccessedTime());
            
            // Check if security context exists in session
            SecurityContext sessionContext = (SecurityContext) session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
            response.put("securityContextInSession", sessionContext != null);
            if (sessionContext != null) {
                Authentication sessionAuth = sessionContext.getAuthentication();
                response.put("sessionAuthExists", sessionAuth != null);
                if (sessionAuth != null) {
                    response.put("sessionAuthName", sessionAuth.getName());
                    response.put("sessionAuthorities", sessionAuth.getAuthorities());
                }
            }
        } else {
            response.put("sessionExists", false);
        }
        
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            response.put("authenticated", true);
            response.put("username", auth.getName());
            response.put("authorities", auth.getAuthorities());
            response.put("hasAdminRole", auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")));
        } else {
            response.put("authenticated", false);
            response.put("hasAdminRole", false);
        }
        
        logger.info("Session status check: {}", response);
        return ResponseEntity.ok(response);
    }
}