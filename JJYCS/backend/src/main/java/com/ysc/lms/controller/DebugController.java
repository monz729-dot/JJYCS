package com.ysc.lms.controller;

import com.ysc.lms.entity.User;
import com.ysc.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/user/{email}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        Map<String, Object> response = new HashMap<>();
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            response.put("found", true);
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            response.put("userType", user.getUserType().toString());
            response.put("status", user.getStatus().toString());
            response.put("emailVerified", user.getEmailVerified());
            response.put("passwordHash", user.getPassword().substring(0, 20) + "..."); // Show first 20 chars
            response.put("memberCode", user.getMemberCode());
        } else {
            response.put("found", false);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/test-password")
    public ResponseEntity<Map<String, Object>> testPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        Map<String, Object> response = new HashMap<>();
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean matches = passwordEncoder.matches(password, user.getPassword());
            
            // Generate a fresh hash for comparison
            String freshHash = passwordEncoder.encode(password);
            
            response.put("userFound", true);
            response.put("passwordMatches", matches);
            response.put("providedPassword", password);
            response.put("storedHashPrefix", user.getPassword().substring(0, 20) + "...");
            response.put("storedHashFull", user.getPassword()); 
            response.put("freshHashForSamePassword", freshHash);
            response.put("userStatus", user.getStatus().toString());
            response.put("encoderClass", passwordEncoder.getClass().getSimpleName());
            
            // Also test if fresh hash matches the password
            response.put("freshHashMatches", passwordEncoder.matches(password, freshHash));
        } else {
            response.put("userFound", false);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/bcrypt/{password}")
    public ResponseEntity<Map<String, String>> generateBcrypt(@PathVariable String password) {
        String hash = passwordEncoder.encode(password);
        Map<String, String> response = new HashMap<>();
        response.put("password", password);
        response.put("hash", hash);
        return ResponseEntity.ok(response);
    }
}