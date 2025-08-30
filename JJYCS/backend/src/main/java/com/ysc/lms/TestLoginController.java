package com.ysc.lms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.ysc.lms.repository.UserRepository;
import com.ysc.lms.entity.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestLoginController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired  
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/users/count")
    public ResponseEntity<Map<String, Object>> getUserCount() {
        try {
            long count = userRepository.count();
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            response.put("message", "Users found in database");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    @GetMapping("/users/admin")
    public ResponseEntity<Map<String, Object>> getAdminUser() {
        try {
            Optional<User> user = userRepository.findByEmail("admin@ycs.com");
            Map<String, Object> response = new HashMap<>();
            if (user.isPresent()) {
                response.put("found", true);
                response.put("email", user.get().getEmail());
                response.put("name", user.get().getName());
                response.put("userType", user.get().getUserType().toString());
            } else {
                response.put("found", false);
                response.put("message", "Admin user not found");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> testLogin(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            Map<String, Object> response = new HashMap<>();
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());
                
                response.put("userFound", true);
                response.put("email", user.getEmail());
                response.put("passwordMatch", passwordMatch);
                response.put("storedPasswordHash", user.getPassword().substring(0, 20) + "...");
                
                if (passwordMatch) {
                    response.put("success", true);
                    response.put("message", "Login successful");
                } else {
                    response.put("success", false);
                    response.put("message", "Password does not match");
                }
            } else {
                response.put("userFound", false);
                response.put("success", false);
                response.put("message", "User not found");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}