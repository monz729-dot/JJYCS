package com.ycs.lms.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {
    
    @GetMapping("/password")
    public Map<String, String> generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "password123";
        String encodedPassword = encoder.encode(rawPassword);
        return Map.of(
            "raw", rawPassword,
            "encoded", encodedPassword
        );
    }
}