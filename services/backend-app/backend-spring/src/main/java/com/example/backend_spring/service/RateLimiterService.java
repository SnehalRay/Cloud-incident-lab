package com.example.backend_spring.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;


import java.time.Instant;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {


    private static final int MAX_REQUESTS = 2;
    private static final long TIME_WINDOW_MS = 2000; // 2 seconds
    private static final String QUEUE_KEY = "jobs:queue";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public boolean isAllowed(String clientId){
        String key = "rate_limiter:" + clientId;

        // atomically increment the request counter for this client in Redis
        // if the key doesn't exist yet, Redis creates it at 0 and returns 1
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            // first request in this window — start the clock by setting an expiry
            // after TIME_WINDOW_MS the key is deleted and the counter resets automatically
            redisTemplate.expire(key, TIME_WINDOW_MS, java.util.concurrent.TimeUnit.MILLISECONDS);
        }

        // allow if within the limit, block if count exceeds MAX_REQUESTS
        return count != null && count <= MAX_REQUESTS;
    }

    public void pushViolationJob(String instanceId, String endpoint) {
        try {
            // build the job payload as JSON — the Rust worker will deserialize this
            String payload = objectMapper.writeValueAsString(Map.of(
                    "type", "rate_limit_violation",
                    "instance_id", instanceId,
                    "endpoint", endpoint,
                    "timestamp", Instant.now().toString()
            ));

            // push to the right end of the Redis list — worker consumes from the left (BLPOP)
            redisTemplate.opsForList().rightPush(QUEUE_KEY, payload);
            log.info("rate_limit_job_queued instance_id={} endpoint={}", instanceId, endpoint);
        } catch (Exception e) {
            log.error("rate_limit_job_queue_failed instance_id={} error={}", instanceId, e.getMessage());
        }
    }


}
