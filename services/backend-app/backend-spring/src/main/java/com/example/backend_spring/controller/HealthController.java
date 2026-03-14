package com.example.backend_spring.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        String db = checkDb(); // checking the health of the database
        String redis = checkRedis(); // checking the health of redis
        String overall = "up".equals(db) && "up".equals(redis) ? "healthy"
                : "down".equals(db) && "down".equals(redis) ? "unhealthy"
                : "degraded"; // overall health is healthy if both db and redis are up, unhealthy if both are down, otherwise degraded
        return ResponseEntity.ok(Map.of("status", overall, "db", db, "redis", redis)); // return the health status of the system along with individual component statuses
    }

    private String checkDb() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class); // simple query to check if the database is responsive
            return "up";
        } catch (Exception e) {
            log.error("db_health_check_failed error={}", e.getMessage()); // log the error if the database health check fails
            return "down";
        }
    }

    private String checkRedis() {
        try {
            redisTemplate.opsForValue().get("__health__"); // simple command to check if redis is responsive, we can use any command here, we just need to check if it throws an exception or not
            return "up";
        } catch (Exception e) {
            log.error("redis_health_check_failed error={}", e.getMessage());
            return "down";
        }
    }
}
