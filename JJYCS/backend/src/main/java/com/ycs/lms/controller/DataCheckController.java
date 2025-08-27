package com.ycs.lms.controller;

import com.ycs.lms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/data-check")
@RequiredArgsConstructor
public class DataCheckController {
    
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final BankAccountRepository bankAccountRepository;
    private final NotificationRepository notificationRepository;
    private final FileRepository fileRepository;
    
    @GetMapping("/summary")
    public Map<String, Object> getDataSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        
        summary.put("users_count", userRepository.count());
        summary.put("orders_count", orderRepository.count());
        summary.put("bank_accounts_count", bankAccountRepository.count());
        summary.put("notifications_count", notificationRepository.count());
        summary.put("files_count", fileRepository.count());
        
        return summary;
    }
    
    @GetMapping("/users/all")
    public Map<String, Object> getAllUsers() {
        return Map.of(
            "users", userRepository.findAll(),
            "count", userRepository.count()
        );
    }
    
    @GetMapping("/orders/all")
    public Map<String, Object> getAllOrders() {
        return Map.of(
            "orders", orderRepository.findAll(),
            "count", orderRepository.count()
        );
    }
    
    @GetMapping("/bank-accounts/all")
    public Map<String, Object> getAllBankAccounts() {
        return Map.of(
            "bank_accounts", bankAccountRepository.findAll(),
            "count", bankAccountRepository.count()
        );
    }
}