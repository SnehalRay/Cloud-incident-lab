package com.example.backend_spring.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
public class StatusController {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    @GetMapping
    public ResponseEntity<Map<String, Object>> status() {
        boolean dbOk = checkDb();
        boolean redisOk = checkRedis();
        String overall = dbOk && redisOk ? "healthy" : dbOk || redisOk ? "degraded" : "unhealthy";

        return ResponseEntity.ok(Map.of(
                "service", "backend",
                "status", overall,
                "dependencies", Map.of(
                        "postgres", Map.of("status", dbOk ? "up" : "down"),
                        "redis", Map.of("status", redisOk ? "up" : "down")
                )
        ));
    }

    private boolean checkDb() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            log.error("db_connectivity_check_failed error={}", e.getMessage());
            return false;
        }
    }

    private boolean checkRedis() {
        try {
            redisTemplate.opsForValue().get("__status__");
            return true;
        } catch (Exception e) {
            log.error("redis_connectivity_check_failed error={}", e.getMessage());
            return false;
        }
    }
}
