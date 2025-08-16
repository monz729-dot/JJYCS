package com.ycs.lms.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * HealthCheck API
 */
@Tag(name = "Health", description = "HealthCheck API")
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Slf4j
public class HealthCheckController {

    /**
     * HealthCheck API
     */
    @PostMapping("/healthCheck")
    public ResponseEntity<Map<String, Object>> healthCheck(
            @RequestBody(required = false) Map<String, Object> request // ← optional
    ) {
        Map<String, Object> res = new HashMap<>();
        res.put("message", "ok");
        return ResponseEntity.ok(res);
    }

}
