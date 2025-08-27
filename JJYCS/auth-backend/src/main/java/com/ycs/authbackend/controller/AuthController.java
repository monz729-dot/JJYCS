package com.ycs.authbackend.controller;

import com.ycs.authbackend.dto.LoginRequest;
import com.ycs.authbackend.dto.LoginResponse;
import com.ycs.authbackend.entity.User;
import com.ycs.authbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:3001", "http://localhost:3000", "http://localhost:3002"})
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid email or password");
            }
            
            User user = userOpt.get();
            
            // For testing: accept "test123" for all users, otherwise use bcrypt
            if ("test123".equals(request.getPassword()) || passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                String token = "mock-jwt-token-" + user.getId();
                
                LoginResponse response = new LoginResponse(
                    token,
                    user.getEmail(),
                    user.getName(),
                    user.getUserType().toString(),
                    user.getMemberCode()
                );
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body("Invalid email or password");
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Login failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth API is working");
    }
}