package com.ycs.lms.controller;

import com.ycs.lms.entity.OrderTracking;
import com.ycs.lms.service.OrderTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/tracking")
@RequiredArgsConstructor
@Slf4j
public class TrackingController {
    
    private final OrderTrackingService trackingService;
    
    /**
     * Get tracking by order number (Public API - No auth required)
     */
    @GetMapping("/order/{orderNumber}")
    public ResponseEntity<Map<String, Object>> getTrackingByOrderNumber(@PathVariable String orderNumber) {
        try {
            List<OrderTracking> trackings = trackingService.getTrackingByOrderNumber(orderNumber);
            if (trackings.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = Map.of(
                "orderNumber", orderNumber,
                "trackings", trackings,
                "currentStatus", trackings.get(trackings.size() - 1).getStatus(),
                "lastUpdate", trackings.get(trackings.size() - 1).getEventTime()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting tracking for order: " + orderNumber, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}